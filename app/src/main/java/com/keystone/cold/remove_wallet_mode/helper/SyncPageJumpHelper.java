package com.keystone.cold.remove_wallet_mode.helper;

import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.sync_jump.AddressDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.FewchaWalletAddressDetector;
import com.keystone.cold.remove_wallet_mode.ui.status.AddressDetectStatus;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

public class SyncPageJumpHelper {

    public static void detect(String walletId, MutableLiveData<AddressDetectStatus> stepMode) {
        Wallet wallet = Wallet.getWalletById(walletId);
        AddressDetector addressDetector = getWalletAddressDetector(wallet);
        if (addressDetector != null) {
            addressDetector.detect(new AddressDetector.Callback() {
                @Override
                public void oneAddress() {
                    stepMode.postValue(AddressDetectStatus.ONE_ADDRESS);
                }

                @Override
                public void moreThanOneAddress() {
                    stepMode.postValue(AddressDetectStatus.MULTI_ADDRESSES);
                }

                @Override
                public void noAddress() {
                    stepMode.postValue(AddressDetectStatus.NO_ADDRESS);
                }
            });
        } else {
            stepMode.postValue(AddressDetectStatus.NO_ADDRESS);
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
