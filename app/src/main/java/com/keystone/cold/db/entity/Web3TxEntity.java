package com.keystone.cold.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ethtxs", indices = {@Index("id")})
public class Web3TxEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String txId;
    private String signedHex;
    private String from;
    private long timeStamp;
    private String belongTo;
    private int txType;
    private String addition;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTxId() {
        return txId;
    }

    public void setTxId(@NonNull String txId) {
        this.txId = txId;
    }

    public String getSignedHex() {
        return signedHex;
    }

    public void setSignedHex(String signedHex) {
        this.signedHex = signedHex;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    @Override
    public String toString() {
        return "Web3TxEntity{" +
                "id=" + id +
                ", txId='" + txId + '\'' +
                ", signedHex='" + signedHex + '\'' +
                ", from='" + from + '\'' +
                ", timeStamp=" + timeStamp +
                ", belongTo='" + belongTo + '\'' +
                ", txType=" + txType +
                ", addition='" + addition + '\'' +
                '}';
    }
}
