package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeoutHeight {

    public static TimeoutHeight from(JSONObject jsonObject) {
        try {
            String revisionHeight = jsonObject.getString("revision_height");
            String revisionNumber = jsonObject.getString("revision_number");
            return new TimeoutHeight(revisionHeight, revisionNumber);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private String revisionHeight;
    private String revisionNumber;

    public TimeoutHeight(String revisionHeight, String revisionNumber) {
        this.revisionHeight = revisionHeight;
        this.revisionNumber = revisionNumber;
    }

    public String getRevisionHeight() {
        return revisionHeight;
    }

    public String getRevisionNumber() {
        return revisionNumber;
    }

    @Override
    public String toString() {
        return "TimeoutHeight{" +
                "revisionHeight='" + revisionHeight + '\'' +
                ", revision_number='" + revisionNumber + '\'' +
                '}';
    }
}
