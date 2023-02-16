package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MsgExec extends Msg {


    public static MsgExec from(JSONObject jsonObject) {
        try {
            String grantee = jsonObject.getString("grantee");
            JSONArray msgArray = jsonObject.getJSONArray("msgs");
            List<Msg> msgs = Msg.getMsgs(msgArray);
            return new MsgExec(grantee, msgs);

        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    private String grantee;
    private List<Msg> msgs;


    public MsgExec(String grantee, List<Msg> msgs) {
        this.type = "Exec";
        this.grantee = grantee;
        this.msgs = msgs;
    }

    public String getGrantee() {
        return grantee;
    }

    public List<Msg> getMsgs() {
        return msgs;
    }

    @Override
    public String toString() {
        return "MsgExec{" +
                "type='" + type + '\'' +
                ", grantee='" + grantee + '\'' +
                ", msgs=" + msgs +
                '}';
    }
}
