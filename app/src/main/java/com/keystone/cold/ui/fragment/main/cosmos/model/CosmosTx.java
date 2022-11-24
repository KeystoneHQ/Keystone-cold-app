package com.keystone.cold.ui.fragment.main.cosmos.model;

import com.keystone.cold.ui.fragment.main.cosmos.model.msg.Msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CosmosTx {

    public static CosmosTx from(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String accountNumber = jsonObject.getString("account_number");
            String chainId = jsonObject.getString("chain_id");
            Fee fee = Fee.from(jsonObject.getJSONObject("fee"));
            String memo = jsonObject.getString("memo");
            JSONArray msgArray = jsonObject.getJSONArray("msgs");
            List<Msg> msgs = Msg.getMsgs(msgArray);
            String sequence = jsonObject.getString("sequence");
            return new CosmosTx(accountNumber, chainId, fee, memo, msgs, sequence);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private String accountNumber;
    private String chainId;
    private Fee fee;
    private String memo;
    private List<Msg> msgs;
    private String sequence;


    public CosmosTx(String accountNumber, String chainId, Fee fee, String memo, List<Msg> msgs, String sequence) {
        this.accountNumber = accountNumber;
        this.chainId = chainId;
        this.fee = fee;
        this.memo = memo;
        this.msgs = msgs;
        this.sequence = sequence;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getChainId() {
        return chainId;
    }

    public Fee getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public List<Msg> getMsgs() {
        return msgs;
    }

    public String getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "CosmosTx{" +
                "accountNumber='" + accountNumber + '\'' +
                ", chainId='" + chainId + '\'' +
                ", fee=" + fee +
                ", memo='" + memo + '\'' +
                ", msgs=" + msgs +
                ", sequence='" + sequence + '\'' +
                '}';
    }
}
