package com.keystone.cold.remove_wallet_mode.helper;


import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AptosAddressGenerator;


public class AddressManager {

    public static void addAddress(final String coinId, final int count, final MutableLiveData<Boolean> status) {
        AddressGenerator addressGenerator = getAddressGenerator(coinId);
        if (addressGenerator == null) {
            status.postValue(false);
            return;
        }
        addressGenerator.generateAddress(count, new AddressGenerator.StatusCallBack() {
            @Override
            public void onSuccess() {
                status.postValue(true);
            }

            @Override
            public void onFail() {
                status.postValue(false);

            }
        });
    }

    private static AddressGenerator getAddressGenerator(String coinId) {
        if (Coins.APTOS.coinId().equals(coinId)) {
            return new AptosAddressGenerator();
        }
        return null;
    }
}
