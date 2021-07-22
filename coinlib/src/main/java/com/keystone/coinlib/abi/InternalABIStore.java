package com.keystone.coinlib.abi;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class InternalABIStore implements ABIStoreEngine {
    private String address;
    private JSONObject bundleMap;

    public InternalABIStore(String address) {
        this.address = address;
        try {
            bundleMap = new JSONObject(readAsset("abi/abiMap.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Contract load() {
        Contract contract = new Contract();
        String abiFile = bundleMap.optString(address);
        if (!TextUtils.isEmpty(abiFile)) {
            contract.setAbi(readAsset("abi/" + abiFile));
            contract.setName(abiFile.replace(".json", ""));
        }
        return contract;
    }
}
