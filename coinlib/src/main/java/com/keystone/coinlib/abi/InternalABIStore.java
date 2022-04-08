package com.keystone.coinlib.abi;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

import java.util.ArrayList;
import java.util.List;

public class InternalABIStore implements ABIStoreEngine {
    @Override
    public List<Contract> load(String address) {
        List<Contract>  contracts = new ArrayList<>();
        String content = readAsset("abi/" + address + ".json");
        if (!TextUtils.isEmpty(content)) {
            try {
                Contract contract = new Contract();
                JSONObject contentJson = new JSONObject(content);
                JSONObject metadataJson = contentJson.getJSONObject("metadata");
                JSONObject outputJson = metadataJson.getJSONObject("output");
                contract.setAbi(outputJson.getString("abi"));
                contract.setName(contentJson.getString("name"));
                contracts.add(contract);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return contracts;
    }
}
