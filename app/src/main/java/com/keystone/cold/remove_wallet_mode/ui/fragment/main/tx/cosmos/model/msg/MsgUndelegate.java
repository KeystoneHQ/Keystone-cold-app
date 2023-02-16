package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.Amount;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgUndelegate extends Msg {

    public static MsgUndelegate from(JSONObject jsonObject) {
        try {
            String delegatorAddress = jsonObject.getString("delegator_address");
            String validatorAddress = jsonObject.getString("validator_address");
            JSONObject amountObject = jsonObject.getJSONObject("amount");
            Amount amount = Amount.from(amountObject);
            return new MsgUndelegate(delegatorAddress, validatorAddress, amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String delegatorAddress;
    private String validatorAddress;
    private Amount amount;

    public MsgUndelegate(String delegatorAddress, String validatorAddress, Amount amount) {
        this.type = "Undelegate";
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
        return "MsgUndelegate{" +
                "type='" + type + '\'' +
                ", delegatorAddress='" + delegatorAddress + '\'' +
                ", validatorAddress='" + validatorAddress + '\'' +
                ", amounts=" + amount +
                '}';
    }
}
