package com.keystone.cold.ui.fragment.main.solana.model;

public class SolTxData {
    private String signature;
    private String rawMessage;
    private String parsedMessage;
    private String signBy;
    private String signatureUR;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getParsedMessage() {
        return parsedMessage;
    }

    public void setParsedMessage(String parsedMessage) {
        this.parsedMessage = parsedMessage;
    }

    public String getSignBy() {
        return signBy;
    }

    public void setSignBy(String signBy) {
        this.signBy = signBy;
    }

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
    }
}
