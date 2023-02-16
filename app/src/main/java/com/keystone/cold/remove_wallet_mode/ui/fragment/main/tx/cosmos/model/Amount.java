package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Amount {

    public static Amount from(JSONObject amountObject) {
        try {
            String amount = amountObject.getString("amount");
            String denom = amountObject.getString("denom");
            return new Amount(amount, denom);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    public String amount;
    public String denom;

    public Amount(String amount, String denom) {
        this.amount = amount;
        this.denom = denom;
    }

    public String getAmount() {
        return amount;
    }

    public String getDenom() {
        return denom;
    }


    @Override
    public String toString() {
        return "Amount{" +
                "amount='" + amount + '\'' +
                ", denom='" + denom + '\'' +
                '}';
    }
}
