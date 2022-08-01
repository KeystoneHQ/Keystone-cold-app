package com.keystone.cold.ui.fragment.main.near.model.actions;

public class DeleteKey extends Action {

    private String publicKey;

    public DeleteKey() {
        priority = 7;
        actionType = "Delete key";
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "DeleteKey{" +
                "actionType='" + actionType + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
