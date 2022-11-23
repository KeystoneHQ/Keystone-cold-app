package com.keystone.cold.ui.fragment.main.cosmos.model.msg;

import com.keystone.cold.ui.fragment.main.cosmos.model.Amount;

import org.json.JSONException;
import org.json.JSONObject;


public class MsgDelegate extends Msg {

    public static MsgDelegate from(JSONObject jsonObject) {
        try {
            String delegatorAddress = jsonObject.getString("delegator_address");
            String validatorAddress = jsonObject.getString("validator_address");
            JSONObject amountObject = jsonObject.getJSONObject("amount");
            Amount amount = Amount.from(amountObject);
            return new MsgDelegate(delegatorAddress, validatorAddress, amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String delegatorAddress;
    private String validatorAddress;
    private Amount amount;

    public MsgDelegate(String delegatorAddress, String validatorAddress, Amount amount) {
        this.type = "Delegate";
        this.delegatorAddress = delegatorAddress;
        this.validatorAddress = validatorAddress;
        this.amount = amount;
    }


    public String getDelegatorAddress() {
        return delegatorAddress;
    }

    public String getValidatorAddress() {
        return validatorAddress;
    }

    public Amount getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "MsgDelegate{" +
                "type='" + type + '\'' +
                ", delegatorAddress='" + delegatorAddress + '\'' +
                ", validatorAddress='" + validatorAddress + '\'' +
                ", amounts=" + amount +
                '}';
    }
}
