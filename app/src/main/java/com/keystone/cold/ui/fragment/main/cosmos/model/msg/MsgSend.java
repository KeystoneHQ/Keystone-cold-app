package com.keystone.cold.ui.fragment.main.cosmos.model.msg;

import com.keystone.cold.ui.fragment.main.cosmos.model.Amount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MsgSend extends Msg {

    public static MsgSend from(JSONObject jsonObject) throws JSONException {

        try {
            String fromAddress = jsonObject.getString("from_address");
            String toAddress = jsonObject.getString("to_address");
            JSONArray amountArray = jsonObject.getJSONArray("amount");
            List<Amount> amounts = new ArrayList<>();
            for (int i = 0; i < amountArray.length(); i++) {
                JSONObject amountObject = amountArray.getJSONObject(i);
                Amount amount = Amount.from(amountObject);
                if (amount != null) {
                    amounts.add(amount);
                }
            }
            return new MsgSend(fromAddress, toAddress, amounts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String fromAddress;
    private String toAddress;
    private List<Amount> amounts;

    public MsgSend(String fromAddress, String toAddress, List<Amount> amounts) {
        this.type = "Send";
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amounts = amounts;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public List<Amount> getAmounts() {
        return amounts;
    }

    @Override
    public String toString() {
        return "MsgSend{" +
                "type='" + type + '\'' +
                ", from_address='" + fromAddress + '\'' +
                ", to_address='" + toAddress + '\'' +
                ", amount=" + amounts +
                '}';
    }


    private String getAmountString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Amount item : amounts) {
            sb.append("{").append(item.amount).append(",").append(item.denom).append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
