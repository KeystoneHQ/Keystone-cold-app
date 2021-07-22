package com.keystone.coinlib.abi;

import com.keystone.coinlib.utils.FileUtil;
import com.keystone.coinlib.utils.SDCardUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SelfDefinedABIStore implements ABIStoreEngine {
    private static final String SELF_DEFINE_TFCARD_PATH = "contracts" + File.separator + "self_define";
    private String address;

    public SelfDefinedABIStore(String address) {
        this.address = address;
    }

    @Override
    public Contract load() {
        Contract contract = new Contract();
        String selfDefineFilePath = SDCardUtil.externalSDCardPath() + File.separator
                + SELF_DEFINE_TFCARD_PATH + File.separator + address + ".json";
        String content = FileUtil.readFromFile(selfDefineFilePath);
        try {
            JSONObject contentJson = new JSONObject(content);
            JSONObject metadataJson = contentJson.getJSONObject("metadata");
            JSONObject outputJson = metadataJson.getJSONObject("output");
            contract.setAbi(outputJson.getString("abi"));
            contract.setName(contentJson.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        contract.setFromTFCard(true);
        return contract;
    }
}
