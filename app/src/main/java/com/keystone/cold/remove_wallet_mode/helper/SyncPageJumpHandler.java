package com.keystone.cold.remove_wallet_mode.helper;

import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.sync_step.AddressDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_step.FewchaWalletAddressDetector;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

public class SyncPageJumpHandler {

    public static void detect(String walletId, MutableLiveData<Integer> stepMode) {
        Wallet wallet = Wallet.getWalletById(walletId);
        AddressDetector addressDetector = getWalletAddressDetector(wallet);
        if (addressDetector != null) {
            addressDetector.detect(new AddressDetector.Callback() {
                @Override
                public void oneAddress() {
                    stepMode.postValue(1);
                }

                @Override
                public void moreThanOneAddress() {
                    stepMode.postValue(2);
                }

                @Override
                public void noAddress() {
                    stepMode.postValue(0);
                }
            });
        } else {
            stepMode.postValue(0);
        }
    }

    private static AddressDetector getWalletAddressDetector(Wallet wallet) {
        //todo  Need to add other types
        switch (wallet) {
            case FEWCHA:
                return new FewchaWalletAddressDetector();
            default:
                return null;
        }
    }

}
