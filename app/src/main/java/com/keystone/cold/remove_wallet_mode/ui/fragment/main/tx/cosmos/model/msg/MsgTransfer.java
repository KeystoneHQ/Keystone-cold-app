package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.Amount;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.TimeoutHeight;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgTransfer extends Msg {


    public static MsgTransfer from(JSONObject jsonObject) {
        try {
            String receiver = jsonObject.getString("receiver");
            String sender = jsonObject.getString("sender");
            String sourceChannel = jsonObject.getString("source_channel");
            String sourcePort = jsonObject.getString("source_port");
            TimeoutHeight timeoutHeight = TimeoutHeight.from(jsonObject.getJSONObject("timeout_height"));
            Amount token = Amount.from(jsonObject.getJSONObject("token"));
            return new MsgTransfer(receiver, sender, sourceChannel, sourcePort, timeoutHeight, token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;


    }

    private String receiver;
    private String sender;
    private String sourceChannel;
    private String sourcePort;
    private TimeoutHeight timeoutHeight;
    private Amount token;

    public MsgTransfer(String receiver, String sender, String sourceChannel, String sourcePort, TimeoutHeight timeoutHeight, Amount token) {
        this.type = "IBC Transfer";
        this.receiver = receiver;
        this.sender = sender;
        this.sourceChannel = sourceChannel;
        this.sourcePort = sourcePort;
        this.timeoutHeight = timeoutHeight;
        this.token = token;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public TimeoutHeight getTimeoutHeight() {
        return timeoutHeight;
    }

    public Amount getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "MsgTransfer{" +
                "type='" + type + '\'' +
                ", receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                ", sourceChannel='" + sourceChannel + '\'' +
                ", sourcePort='" + sourcePort + '\'' +
                ", timeoutHeight=" + timeoutHeight +
                ", token=" + token +
                '}';
    }
}
