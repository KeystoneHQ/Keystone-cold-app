package com.keystone.cold.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ethtxs", indices = {@Index("txId")})
public class FeeMarketTxEntity extends ETHTxEntity {

    @PrimaryKey
    @NonNull
    private String estimatedFee;
    private String maxFee;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String gasLimit;

    public String getEstimatedFee() {
        return estimatedFee;
    }

    public void setEstimatedFee(String estimatedFee) {
        this.estimatedFee = estimatedFee;
    }

    public String getMaxFee() {
        return maxFee;
    }

    public void setMaxFee(String maxFee) {
        this.maxFee = maxFee;
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public String getMaxFeePerGas() {
        return maxFeePerGas;
    }

    public String getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    @NonNull
    @Override
    public String toString() {
        return "TxEntity{" +
                "txId='" + getTxId() + '\'' +
                ", coinId='" + getCoinId() + '\'' +
                ", coinCode='" + getCoinCode() + '\'' +
                ", amount='" + getAmount() + '\'' +
                ", from='" + getFrom() + '\'' +
                ", to='" + getTo() + '\'' +
                ", estimatedFee='" + getEstimatedFee() + '\'' +
                ", maxFee='" + getMaxFee() + '\'' +
                ", maxFeePerGas='" + getMaxFeePerGas() + '\'' +
                ", maxPriorityFeePerGas='" + getMaxPriorityFeePerGas() + '\'' +
                ", signedHex='" + getSignedHex() + '\'' +
                ", timeStamp=" + getTimeStamp() +
                ", memo='" + getMemo() + '\'' +
                ", signId='" + getSignId() + '\'' +
                ", belongTo='" + getBelongTo() + '\'' +
                '}';
    }
}
