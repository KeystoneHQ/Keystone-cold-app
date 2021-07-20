package com.keystone.cold.ui.fragment.main.scan.scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanResult {
    private ScanResultTypes type;
    private String data;

    public static String createJSON(ScanResultTypes type, String data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type.name());
        json.put("data", data);
        return json.toString();
    }

    public static ScanResult newInstance(String json) throws JSONException {
        JSONObject result = new JSONObject(json);
        return new ScanResult(ScanResultTypes.valueOf(result.getString("type")), result.getString("data"));
    }

    public ScanResult(ScanResultTypes type, String data) {
        this.type = type;
        this.data = data;
    }

    public ScanResultTypes getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public Object resolve() {
        return this.type.resolveURHex(this.data);
    }
}
