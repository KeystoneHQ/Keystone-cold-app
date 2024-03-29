package com.keystone.cold.ui.fragment.main.cosmos.model;


public class CosmosTxData {
    private String signature;
    private String rawMessage;
    private String parsedMessage;
    private String signatureUR;
    private CosmosTx cosmosTx;



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

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
    }

    public CosmosTx getCosmosTx() {
        return cosmosTx;
    }

    public void setCosmosTx(CosmosTx cosmosTx) {
        this.cosmosTx = cosmosTx;
    }
}
