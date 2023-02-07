package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey;

public class AccessKey {
    private long nonce;
    private KeyPermission keyPermission;

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public KeyPermission getKeyPermission() {
        return keyPermission;
    }

    public void setKeyPermission(KeyPermission keyPermission) {
        this.keyPermission = keyPermission;
    }

    @Override
    public String toString() {
        return "AccessKey{" +
                "nonce=" + nonce +
                ", keyPermission=" + keyPermission +
                '}';
    }
}
