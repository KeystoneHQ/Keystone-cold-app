package com.keystone.cold.db.viewmodel;

import com.keystone.cold.db.entity.CoinEntity;
public class CoinModel {
    private final CoinEntity entity;

    public CoinModel(CoinEntity entity) {
        this.entity = entity;
    }

    public String getTag() {
        switch (entity.getCoinCode()){
            case "BTC_LEGACY":
            case "BTC_TESTNET_LEGACY":
                return "Legacy";
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_NATIVE_SEGWIT":
                return "Native Segwit";
            default:
                return "Nested Segwit";
        }
    }
    public boolean hasTag() {
        switch (entity.getCoinCode()){
            case "BTC_LEGACY":
            case "BTC_TESTNET_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_NATIVE_SEGWIT":
            case "BTC":
            case "BTC_TESTNET_SEGWIT":
                return true;
            default:
                return false;
        }
    }

    public long getId() {
        return entity.getId();
    }

    public String getBelongTo() {
        return entity.getBelongTo();
    }

    public String getCoinId() {
        return entity.getCoinId();
    }

    public String getName() {
        return entity.getName();
    }

    public String getCoinCode() {
        return entity.getCoinCode();
    }

    public String getDisplayName() {
        switch (entity.getCoinCode()) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
                return "BTC";
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
                return "XTN";
        }
        return entity.getDisplayName();
    }

    public int getIconResId() {
        return entity.getIconResId();
    }

    public int getAddressCount(){
        return entity.getAddressCount();
    }

    public  boolean isShow() {
        return entity.isShow();
    }

    public String getExPub() {
        return entity.getExPub();
    }

    public int getIndex() {
        return entity.getIndex();
    }
}
