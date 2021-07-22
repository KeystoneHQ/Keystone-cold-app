package com.keystone.coinlib.abi;

import android.text.TextUtils;

public class AbiLoadManager {
    private String address;
    private Contract contract;

    public AbiLoadManager(String address) {
        this.address = address.toLowerCase();
        contract = new Contract();
    }

    public Contract loadAbi() {
        if (TextUtils.isEmpty(address)) {
            return contract;
        }
        InternalABIStore internalABIStore = new InternalABIStore(address);
        return internalABIStore.load();
    }
}
