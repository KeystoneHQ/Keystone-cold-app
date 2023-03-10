package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.UR;

public class MetamaskViewModel extends AndroidViewModel {

    public MetamaskViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UR> generateSyncUR() {
        String code = Utilities.getCurrentEthAccount(getApplication());
        ETHAccount ethAccount = ETHAccount.ofCode(code);
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur;
            switch (ethAccount) {
                case LEDGER_LIVE: {
                    ur = URRegistryHelper.generateCryptoAccountForLedgerLive(0, 10).toUR();
                    break;
                }
                case LEDGER_LEGACY: {
                    ur = URRegistryHelper.generateCryptoHDKeyForLedgerLegacy().toUR();
                    break;
                }
                default: {
                    ur = URRegistryHelper.generateCryptoHDKeyForETHStandard().toUR();
                }
            }
            data.postValue(ur);
        });
        return data;
    }
}
