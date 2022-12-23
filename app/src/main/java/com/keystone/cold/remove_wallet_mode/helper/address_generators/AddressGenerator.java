package com.keystone.cold.remove_wallet_mode.helper.address_generators;

public interface AddressGenerator {

    interface StatusCallBack {
        void onSuccess();
        void onFail();
    }

    void generateAddress(int count, StatusCallBack statusCallBack);
}
