package com.keystone.coinlib.abi;

import android.text.TextUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbiLoadManager {
    private String address;
    private List<ABIStoreEngine> abiStoreEngineList;

    public AbiLoadManager(String address) {
        this.address = address.toLowerCase();
        abiStoreEngineList = Arrays.asList(
                new InternalABIStore(this.address),
                new TFCardABIStore(this.address),
                new SelfDefinedABIStore(this.address));
    }

    public AbiLoadManager(List<ABIStoreEngine> abiStoreEngines, String address) {
        this.address = address.toLowerCase();
        abiStoreEngineList = abiStoreEngines;
    }

    public Contract loadAbi() {
        Contract contract = new Contract();
        if (TextUtils.isEmpty(address)) {
            return contract;
        }
        for (ABIStoreEngine abiStoreEngine : abiStoreEngineList) {
            Contract load = abiStoreEngine.load();
            if (!load.isEmpty()) {
                contract = load;
                break;
            }
        }
        return contract;
    }
}