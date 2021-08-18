package com.keystone.coinlib.abi;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class InternalABIStore implements ABIStoreEngine {
    @Override
    public Contract load(String address) {
        Contract contract = new Contract();
        String content = readAsset("abi/" + address + ".json");
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObject contentJson = new JSONObject(content);
                JSONObject metadataJson = contentJson.getJSONObject("metadata");
                JSONObject outputJson = metadataJson.getJSONObject("output");
                contract.setAbi(outputJson.getString("abi"));
                contract.setName(contentJson.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return contract;
    }
}
