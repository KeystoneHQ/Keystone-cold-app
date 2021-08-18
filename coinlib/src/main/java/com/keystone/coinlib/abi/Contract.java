package com.keystone.coinlib.abi;

import org.json.JSONException;
import org.json.JSONObject;

public class Contract {
    private String name;

    private String abi;

    private String metadata;

    private boolean isFromTFCard;

    public Contract() {
    }

    public Contract(String name, String abi) {
        this.name = name;
        this.abi = abi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
        this.abi = createAbi();
    }

    private String createAbi() {
        String abi = null;
        if (metadata == null) {
            return abi;
        }
        try {
            JSONObject metaData = new JSONObject(metadata);
            JSONObject output = metaData.getJSONObject("output");
            abi = output.getString("abi");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return abi;
    }

    public boolean isFromTFCard() {
        return isFromTFCard;
    }

    public void setFromTFCard(boolean fromTFCard) {
        isFromTFCard = fromTFCard;
    }

    public boolean isEmpty() {
        return abi == null;
    }
}