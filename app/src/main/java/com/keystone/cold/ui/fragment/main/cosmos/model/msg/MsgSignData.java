package com.keystone.cold.ui.fragment.main.cosmos.model.msg;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgSignData extends Msg {

    public static MsgSignData from(JSONObject jsonObject) {
        try {
            String signer = jsonObject.getString("signer");
            String data = jsonObject.getString("data");
            return new MsgSignData(signer, data);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private String signer;
    private String data;

    public MsgSignData(String signer, String data) {
        this.type = "Message";
        this.signer = signer;
        this.data = data;
    }


    public String getSigner() {
        return signer;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MsgSignData{" +
                "type='" + type + '\'' +
                ", signer='" + signer + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
