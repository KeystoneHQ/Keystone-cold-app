package com.keystone.cold.viewmodel.tx;

public class SignState {
    public static final String STATE_NONE = "";
    public static final String STATE_SIGNING = "signing";
    public static final String STATE_SIGN_FAIL = "signing_fail";
    public static final String STATE_SIGN_SUCCESS = "signing_success";

    private final String status;
    private final String txId;
    private final String signature;

    public SignState(String status) {
        this.status = status;
        this.txId = null;
        this.signature = null;
    }

    public SignState(String status, String txId) {
        this.status = status;
        this.txId = txId;
        this.signature = null;
    }

    public SignState(String status, String txId, String signature){
        this.status = status;
        this.txId = txId;
        this.signature = signature;
    }


    public String getStatus() {
        return status;
    }

    public String getTxId() {
        return txId;
    }

    public String getSignature() {
        return signature;
    }
}
