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

import com.keystone.cold.model.Tx;
import com.keystone.cold.protocol.builder.SignTxResultBuilder;

@Entity(tableName = "txs", indices = {@Index("txId")})

public class TxEntity implements Tx {

    @PrimaryKey
    @NonNull
    private String txId;
    private String coinId;
    private String coinCode;
    private String amount;
    private String from;
    private String to;
    private String fee;
    private String signedHex;
    private long timeStamp;
    private String memo;
    private String signId;
    private String belongTo;

    /**
     * TxEntity类的额外信息，内部是JSON
     * 目前仅用于Solana Near Polkadot Bitcion Cosmos相关币种 其他币种暂时没有用到
     *
     * @Solana {
     * {
     * "additioins":{
     * "coin":"solana",
     * "addition":{
     * "signature":"adfas",
     * "raw_message":"raw_message",
     * "parsed_message":"parsed_message",
     * "sign_by":"bip44"
     * }
     * }
     * }
     * }
     * @coin 表示币种
     * @addition 表示附加信息
     * @signature 表示签名
     * @raw_message 表示原始信息
     * @parsed_message 表示解析后的信息
     * @Near {
     * {
     * "additioins":{
     * "coin":"near",
     * "addition":{
     * "signature":"adfas",
     * "raw_message":"raw_message",
     * "parsed_message":"parsed_message",
     * "sign_by":"bip44",
     * "sign_batch_info":{
     * "batch_id":"xxx",
     * "order":"0"
     * }
     * }
     * }
     * }
     * }
     * @Polkadot {
     * addition: {
     * "raw_message": "raw_message",
     * "parsed_message": "",
     * }
     * }
     * @coin 表示币种
     * @addition 表示附加信息
     * @signature 表示签名
     * @raw_message 表示原始信息
     * @parsed_message 表示解析后的信息
     * @sign_batch_info 表示同一批次交易的签名信息 （考虑后期可能支持一次签多笔交易，预留该字段）
     * @batch_id 批次id（同一批次中所有交易id的Keccak-256 hash）
     * @order 这一批次的第几个交易
     * @Bitcoin {
     * coin: "bitcoin",
     * addition: {
     * "raw_message": "raw_message",
     * "parsed_message": {
     * "_version": 0,
     * "inputs": [],
     * "outputs": [],
     * "fee": "",
     * }
     * }
     * }
     * @Cosmos {
     * {
     * "additioins":{
     * "coin":"xxx",
     * "addition":{
     * "signature":"adfas",
     * "raw_message":"raw_message",
     * "parsed_message":"parsed_message",
     * "sign_mode": "evm",  //evm or cosmos
     * "chain_id": "injective-1" //  cosmos chain id
     * }
     * }
     * }
     * }
     */
    private String addition;

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
        return coinId;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public String getCoinCode() {
        return coinCode;
    }

    @Override
    public String getDisplayName() {
        if (coinCode.startsWith("BTC")) {
            return "BTC";
        }
        return coinCode;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
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

    @Override
    public String getMemo() {
        return memo;
    }

    public void setSignId(String signId) {
        this.signId = signId;
    }

    @Override
    public String getSignId() {
        return signId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setAddition(String json) {
        this.addition = json;
    }

    public String getAddition() {
        return this.addition;
    }

    public String getSignResult() {
        if (signedHex == null || signId == null || txId.isEmpty()) return null;
        SignTxResultBuilder signTxResult = new SignTxResultBuilder();
        signTxResult.setRawTx(signedHex)
                .setSignId(signId)
                .setTxId(txId);
        return signTxResult.build();
    }

    @Override
    public String toString() {
        return "TxEntity{" +
                "txId='" + txId + '\'' +
                ", coinId='" + coinId + '\'' +
                ", coinCode='" + coinCode + '\'' +
                ", amount='" + amount + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", fee='" + fee + '\'' +
                ", signedHex='" + signedHex + '\'' +
                ", timeStamp=" + timeStamp +
                ", memo='" + memo + '\'' +
                ", signId='" + signId + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", addition='" + addition + '\'' +
                '}';
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
}
