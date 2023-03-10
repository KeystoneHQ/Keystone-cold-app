package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoMultiAccounts;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class CoreWalletViewModel extends AndroidViewModel {

    public CoreWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UR> generateSyncUR() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur = generateCryptoMultiAccounts(ETHAccount.BIP44_STANDARD).toUR();
            data.postValue(ur);
        });
        return data;
    }

    // For coreWallet is using M/44'/60'/0' to derive bech32 bitcoin addresses, so we do not need to export bitcoin keys.
    private CryptoMultiAccounts generateCryptoMultiAccounts(ETHAccount ethAccount) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        List<CryptoHDKey> cryptoHDKeyList = new ArrayList<>();
        switch (ethAccount) {
            case LEDGER_LEGACY: {
                CryptoHDKey ledgerLegacy = URRegistryHelper.generateCryptoHDKeyForLedgerLegacy();
                cryptoHDKeyList.add(ledgerLegacy);
                break;
            }
            case LEDGER_LIVE: {
                List<Integer> list = new ArrayList<>(10);
                for (int i = 0; i < 10; i++) {
                    list.add(i);
                }
                List<CryptoHDKey> ledgerLive = URRegistryHelper.generateCryptoHDKeysForLedgerLive(list);
                cryptoHDKeyList.addAll(ledgerLive);
                break;
            }
            default: {
                CryptoHDKey standard = URRegistryHelper.generateCryptoHDKeyForETHStandard();
                cryptoHDKeyList.add(standard);
            }
        }
        return new CryptoMultiAccounts(masterFingerprint, cryptoHDKeyList, URRegistryHelper.KEY_NAME);
    }
}
