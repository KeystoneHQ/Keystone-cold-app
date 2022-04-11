package com.keystone.coinlib.abi;

import android.text.TextUtils;

import java.util.ArrayList;
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

    public List<Contract>  loadAbi() {
        List<Contract> contracts = new ArrayList<>();
        if (TextUtils.isEmpty(address)) {
            return contracts;
        }
        for (ABIStoreEngine abiStoreEngine : abiStoreEngineList) {
            contracts.addAll(abiStoreEngine.load(address));
            if (!contracts.isEmpty()){
                return contracts;
            }
        }
        return contracts;
    }
}