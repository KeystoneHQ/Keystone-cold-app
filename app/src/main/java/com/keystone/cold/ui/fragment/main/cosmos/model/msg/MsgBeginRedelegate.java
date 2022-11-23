package com.keystone.cold.ui.fragment.main.cosmos.model.msg;

import com.keystone.cold.ui.fragment.main.cosmos.model.Amount;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgBeginRedelegate extends Msg {

    public static MsgBeginRedelegate from(JSONObject jsonObject) {
        try {
            String delegatorAddress = jsonObject.getString("delegator_address");
            String validatorDstAddress = jsonObject.getString("validator_dst_address");
            String validatorSrcAddress = jsonObject.getString("validator_src_address");
            JSONObject amountObject = jsonObject.getJSONObject("amount");
            Amount amount = Amount.from(amountObject);
            return new MsgBeginRedelegate(delegatorAddress, validatorDstAddress, validatorSrcAddress, amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String delegatorAddress;
    private String validatorDstAddress;
    private String validatorSrcAddress;
    private Amount amount;

    public MsgBeginRedelegate(String delegatorAddress, String validatorDstAddress, String validatorSrcAddress, Amount amount) {
        this.type = "Redelegate";
        this.delegatorAddress = delegatorAddress;
        this.validatorDstAddress = validatorDstAddress;
        this.validatorSrcAddress = validatorSrcAddress;
        this.amount = amount;
    }

    public String getDelegatorAddress() {
        return delegatorAddress;
    }

    public String getValidatorDstAddress() {
        return validatorDstAddress;
    }

    public String getValidatorSrcAddress() {
        return validatorSrcAddress;
    }

    public Amount getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "MsgBeginRedelegate{" +
                "type='" + type + '\'' +
                ", delegatorAddress='" + delegatorAddress + '\'' +
                ", validatorDstAddress='" + validatorDstAddress + '\'' +
                ", validatorSrcAddress='" + validatorSrcAddress + '\'' +
                ", amount=" + amount +
                '}';
    }
}
