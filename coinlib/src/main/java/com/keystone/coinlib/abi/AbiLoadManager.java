package com.keystone.coinlib.abi;

import android.text.TextUtils;

import java.util.List;

public class AbiLoadManager {
    private String address;
    private List<ABIStoreEngine> abiStoreEngineList;

    public AbiLoadManager(String address) {
        this.address = address.toLowerCase();
        abiStoreEngineList.add(new InternalABIStore(this.address));
        abiStoreEngineList.add(new TFCardABIStore(this.address));
        abiStoreEngineList.add(new SelfDefinedABIStore(this.address));
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
