package com.keystone.cold.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ethmsgs", indices = {@Index("id")})
public class ETHMsgDBEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String msgId;
    private String signature;
    private long timeStamp;
    private String addition;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(@NonNull String msgId) {
        this.msgId = msgId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setAddition(String json){
        this.addition = json;
    }

    public String getAddition(){
        return this.addition;
    }

}
