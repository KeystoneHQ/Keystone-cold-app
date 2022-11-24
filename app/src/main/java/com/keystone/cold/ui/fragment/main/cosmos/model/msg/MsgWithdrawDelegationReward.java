package com.keystone.cold.ui.fragment.main.cosmos.model.msg;


import org.json.JSONException;
import org.json.JSONObject;

public class MsgWithdrawDelegationReward extends Msg {

    public static MsgWithdrawDelegationReward from(JSONObject jsonObject) {
        try {
            String delegatorAddress = jsonObject.getString("delegator_address");
            String validatorAddress = jsonObject.getString("validator_address");
            return new MsgWithdrawDelegationReward(delegatorAddress, validatorAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String delegatorAddress;
    private String validatorAddress;

    public MsgWithdrawDelegationReward(String delegatorAddress, String validatorAddress) {
        this.type = "Withdraw Rewards";
        this.delegatorAddress = delegatorAddress;
        this.validatorAddress = validatorAddress;
    }

    public String getDelegatorAddress() {
        return delegatorAddress;
    }

    public String getValidatorAddress() {
        return validatorAddress;
    }


    @Override
    public String toString() {
        return "MsgWithdrawDelegationReward{" +
                "type='" + type + '\'' +
                ", delegatorAddress='" + delegatorAddress + '\'' +
                ", validatorAddress='" + validatorAddress + '\'' +
                '}';
    }
}
