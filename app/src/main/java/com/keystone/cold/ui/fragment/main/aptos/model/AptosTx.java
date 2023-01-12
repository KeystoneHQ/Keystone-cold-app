package com.keystone.cold.ui.fragment.main.aptos.model;

import com.keystone.cold.ui.fragment.main.aptos.model.payload.PayLoad;

public class AptosTx {

    private static final String ADDRESS_PREFIX = "0x";

    private long chainId;
    private long sequenceNumber;
    private String sender;
    private PayLoad payLoad;
    private long gasUnitPrice;
    private long maxGasAmount;
    private long expirationTimestampSecs;

    private String signatureUR;

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSender() {
        if (sender != null && !sender.startsWith(ADDRESS_PREFIX)) {
            return ADDRESS_PREFIX + sender;
        }
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public PayLoad getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(PayLoad payLoad) {
        this.payLoad = payLoad;
    }

    public long getGasUnitPrice() {
        return gasUnitPrice;
    }

    public void setGasUnitPrice(long gasUnitPrice) {
        this.gasUnitPrice = gasUnitPrice;
    }

    public long getMaxGasAmount() {
        return maxGasAmount;
    }

    public void setMaxGasAmount(long maxGasAmount) {
        this.maxGasAmount = maxGasAmount;
    }

    public long getExpirationTimestampSecs() {
        return expirationTimestampSecs;
    }

    public void setExpirationTimestampSecs(long expirationTimestampSecs) {
        this.expirationTimestampSecs = expirationTimestampSecs;
    }

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
    }

    @Override
    public String toString() {
        return "AptosTx{" +
                "chainId=" + chainId +
                ", sequenceNumber=" + sequenceNumber +
                ", sender='" + sender + '\'' +
                ", payLoad=" + payLoad +
                ", gasUnitPrice=" + gasUnitPrice +
                ", maxGasAmount=" + maxGasAmount +
                ", expirationTimestampSecs=" + expirationTimestampSecs +
                '}';
    }
}
