package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Msg {
    protected String type;

    public String getType() {
        return type;
    }


    public static List<Msg> getMsgs(JSONArray msgArray) {
        List<Msg> msgs = new ArrayList<>();
        for (int i = 0; i < msgArray.length(); i++) {
            try {
                JSONObject msgContainer = msgArray.getJSONObject(i);
                msgs.add(getMsg(msgContainer));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

        }
        return msgs;
    }

    private static Msg getMsg(JSONObject msgContainer) {
        try {
            String type = msgContainer.getString("type");
            JSONObject value = msgContainer.getJSONObject("value");
            if (type.endsWith("MsgSend")) {
                return MsgSend.from(value);
            } else if (type.endsWith("MsgDelegate")) {
                return MsgDelegate.from(value);
            } else if (type.endsWith("MsgUndelegate")) {
                return MsgUndelegate.from(value);
            } else if (type.endsWith("MsgBeginRedelegate")) {
                return MsgBeginRedelegate.from(value);
            } else if (type.endsWith("MsgWithdrawDelegationReward") || type.endsWith("MsgWithdrawDelegatorReward")) {
                return MsgWithdrawDelegationReward.from(value);
            } else if (type.endsWith("MsgTransfer")) {
                return MsgTransfer.from(value);
            } else if (type.endsWith("MsgVote")) {
                return MsgVote.from(value);
            } else if (type.endsWith("MsgExec")) {
                return MsgExec.from(value);
            } else if (type.endsWith("MsgSignData")) {
                return MsgSignData.from(value);
            } else {
                return new UnsupportMsg(type, value.toString());
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
