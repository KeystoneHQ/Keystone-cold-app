package com.keystone.cold.ui.fragment.main.near.model;

import com.keystone.cold.ui.fragment.main.near.model.actions.Action;
import com.keystone.cold.ui.fragment.main.near.model.parser.NearTxParser;


import java.util.List;

public class NearTx {

    public static final String MAIN_NET_SUFFIX = "near";
    public static final String TEST_NET_SUFFIX = "testnet";

    public static NearTx from(String formattedJsonStr) {
        return NearTxParser.parse(formattedJsonStr);
    }

    private String signerId;
    private String receiverId;
    private String publicKey;
    private long nonce;
    private List<Action> actions;

    @Override
    public String toString() {
        return "NearTx{" +
                "signerId='" + signerId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", nonce=" + nonce +
                ", actions=" + actionsToString() +
                '}';
    }

    private String actionsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Action action : actions) {
            sb.append(action.toString()).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public void setSignerId(String signerId) {
        this.signerId = signerId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getSignerId() {
        return signerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public long getNonce() {
        return nonce;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getNetWork() {
        if (signerId.endsWith(MAIN_NET_SUFFIX) || receiverId.endsWith(MAIN_NET_SUFFIX)) {
            return "NEAR Mainnet";
        } else if (signerId.endsWith(TEST_NET_SUFFIX) || receiverId.endsWith(TEST_NET_SUFFIX)) {
            return "NEAR Testnet";
        }
        return "Unknown";
    }
}
