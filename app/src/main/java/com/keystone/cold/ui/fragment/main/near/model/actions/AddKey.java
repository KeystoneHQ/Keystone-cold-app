package com.keystone.cold.ui.fragment.main.near.model.actions;

import com.keystone.cold.ui.fragment.main.near.model.actions.accesskey.AccessKey;

public class AddKey extends Action {
    private String publicKey;
    private AccessKey accessKey;

    public AddKey() {
        priority = 6;
        actionType = "Add Key";
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public AccessKey getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(AccessKey accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public String toString() {
        return "AddKey{" +
                "priority=" + priority +
                ", actionType='" + actionType + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", accessKey=" + accessKey +
                '}';
    }
}
