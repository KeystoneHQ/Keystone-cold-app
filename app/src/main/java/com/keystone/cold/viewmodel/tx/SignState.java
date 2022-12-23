package com.keystone.cold.viewmodel.tx;

public class SignState {
    private final String status;
    private final String txId;

    public SignState(String status, String txId) {
        this.status = status;
        this.txId = txId;
    }

    public String getStatus() {
        return status;
    }

    public String getTxId() {
        return txId;
    }
}
