/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.db.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.model.Tx;
import com.keystone.cold.viewmodel.WatchWallet;

@Entity(tableName = "ethtxs", indices = {@Index("txId")})
public class GenericETHTxEntity implements Tx {

    @PrimaryKey
    @NonNull
    private String txId;
    private String signedHex;
    private String from;
    private long timeStamp;
    private String addition;



    private String amount;
    private int chainId;
    private String to;
    private String fee;
    private String memo;
    private String belongTo;
    private String estimatedFee;
    private String maxFee;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String gasLimit;
    private String signature;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

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

    @Override
    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    @NonNull
    @Override
    public String getTxId() {
        return txId;
    }

    public void setTxId(@NonNull String txId) {
        this.txId = txId;
    }

    @Override
    public String getCoinId() {
        return Coins.ETH.coinId();
    }

    @Override
    public String getCoinCode() {
        return Coins.ETH.coinCode();
    }

    @Override
    public String getDisplayName() {
        return Coins.ETH.coinCode();
    }

    @Override
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    @Override
    public String getSignedHex() {
        return signedHex;
    }

    public void setSignedHex(String signedHex) {
        this.signedHex = signedHex;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    @Override
    public String getMemo() {
        return memo;
    }


    @Override
    public String getSignId() {
        return WatchWallet.METAMASK_SIGN_ID;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean filter(String s) {

        if (TextUtils.isEmpty(s)) {
            return true;
        }
        s = s.toLowerCase();
        return from.toLowerCase().contains(s)
                || to.toLowerCase().contains(s)
                || txId.toLowerCase().contains(s)
                || memo.toLowerCase().contains(s);
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
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
                ", addition='" + getAddition() + '\'' +
                '}';
    }
}
