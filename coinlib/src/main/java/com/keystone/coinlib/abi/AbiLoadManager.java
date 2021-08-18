package com.keystone.coinlib.abi;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class AbiLoadManager {
    private String address;
    private static List<ABIStoreEngine> abiStoreEngineList;

    static {
        abiStoreEngineList = Arrays.asList(
                new InternalABIStore(),
                new TFCardABIStore(),
                new SelfDefinedABIStore());
    }

    public AbiLoadManager(String address) {
        this.address = address.toLowerCase();
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
            Contract load = abiStoreEngine.load(address);
            if (!load.isEmpty()) {
                contract = load;
                break;
            }
        }
        return contract;
    }
}