package com.keystone.coinlib.abi;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class RecognizeAddressManager {
    private String address;
    private static List<ABIStoreEngine> abiStoreEngineList;

    static {
        abiStoreEngineList = Arrays.asList(
                new TFCardENSStore(),
                new InternalABIStore(),
                new TFCardABIStore(),
                new SelfDefinedABIStore());
    }

    public RecognizeAddressManager(String address) {
        this.address = address.toLowerCase();
    }

    public String recognizeAddress() {
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        for (ABIStoreEngine abiStoreEngine : abiStoreEngineList) {
            String name = abiStoreEngine.load(address).getName();
            if (!TextUtils.isEmpty(name)) {
                return name;
            }
        }
        return null;
    }
}