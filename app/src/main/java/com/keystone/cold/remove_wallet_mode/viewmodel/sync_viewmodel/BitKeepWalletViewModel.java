package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoMultiAccounts;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class BitKeepWalletViewModel extends AndroidViewModel {
    public static final String BTCLegacyPath = "M/44'/0'/0'";
    public static final String BTCNestedSegwitPath = "M/49'/0'/0'";
    public static final String BTCNativeSegwitPath = "M/84'/0'/0'";

    private final String[] btcPaths = {BTCLegacyPath, BTCNestedSegwitPath, BTCNativeSegwitPath};

    private List<String> openedCoins = new ArrayList<>();

    public BitKeepWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UR> generateSyncUR() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur = generateCryptoMultiAccounts().toUR();
            data.postValue(ur);
        });
        return data;
    }

    public void setOpenedCoins(List<String> openedCoins) {
        this.openedCoins.clear();
        if (openedCoins != null) {
            this.openedCoins.addAll(openedCoins);
        }
        if (this.openedCoins.isEmpty()) {
            this.openedCoins.add(Coins.ETH.coinId());
            this.openedCoins.add(Coins.BTC.coinId());
        }
    }

    private CryptoMultiAccounts generateCryptoMultiAccounts() {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        List<CryptoHDKey> cryptoHDKeyList = new ArrayList<>();
        if (openedCoins.contains(Coins.ETH.coinId())) {
            cryptoHDKeyList.addAll(generateCryptoHDKeysForEthereum());
        }
        if (openedCoins.contains(Coins.BTC.coinId())) {
            cryptoHDKeyList.addAll(generateCryptoHDKeysForBitcoin());
        }
        return new CryptoMultiAccounts(masterFingerprint, cryptoHDKeyList, URRegistryHelper.KEY_NAME);
    }


    private List<CryptoHDKey> generateCryptoHDKeysForEthereum() {
        ETHAccount account = ETHAccount.ofCode(Utilities.getCurrentEthAccount(getApplication()));
        return generateCryptoHDKeysForEthereumByETHAccount(account);
    }

    private List<CryptoHDKey> generateCryptoHDKeysForEthereumByETHAccount(ETHAccount ethAccount) {
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
        return cryptoHDKeyList;
    }

    private List<CryptoHDKey> generateCryptoHDKeysForBitcoin() {
        List<CryptoHDKey> cryptoHDKeyList = new ArrayList<>();
        for (String bitcoinPath : btcPaths) {
            CryptoHDKey bitcoin = generateCryptoHDKeyForBitcoin(bitcoinPath);
            cryptoHDKeyList.add(bitcoin);
        }
        return cryptoHDKeyList;
    }

    private CryptoHDKey generateCryptoHDKeyForBitcoin(String path) {
        return URRegistryHelper.generateCryptoHDKey(path, 0);
    }
}