package com.keystone.cold.db.viewmodel;

import com.keystone.cold.db.entity.CoinEntity;

public class CoinModel {
    private String tag;
    private boolean hasTag;
    private final long id;
    private final String belongTo;
    private final String coinId;
    private final String coinCode;
    private final String displayCoinCode;
    private final String name;
    private final String displayName;
    private int addressCount;
    private final boolean isShow;
    private final String exPub;
    private final int index;

    public CoinModel(CoinEntity entity) {
        this.id = entity.getId();
        this.belongTo = entity.getBelongTo();
        this.coinId = entity.getCoinId();
        this.coinCode = entity.getCoinCode();
        this.addressCount = entity.getAddressCount();
        this.isShow = entity.isShow();
        this.exPub = entity.getExPub();
        this.index = entity.getIndex();
        this.displayName = entity.getDisplayName();
        this.name = entity.getName();
        this.displayCoinCode = entity.getCoinCode();
        this.setTag(entity);
    }

    public CoinModel(long id, String belongTo, String coinId, String coinCode, String displayCoinCode, String name, String displayName, int addressCount, boolean isShow, String exPub, int index) {
        this.id = id;
        this.belongTo = belongTo;
        this.coinId = coinId;
        this.coinCode = coinCode;
        this.displayCoinCode = displayCoinCode;
        this.name = name;
        this.displayName = displayName;
        this.addressCount = addressCount;
        this.isShow = isShow;
        this.exPub = exPub;
        this.index = index;
    }

    public static CoinModel fake(CoinEntity entity, String displayCoinCode, String displayName) {
        return new CoinModel(entity.getId(), entity.getBelongTo(), entity.getCoinId(), entity.getCoinCode(), displayCoinCode, entity.getName(), displayName, entity.getAddressCount(), entity.isShow(), entity.getExPub(), entity.getIndex());
    }

    private void setTag(CoinEntity entity) {
        switch (entity.getCoinCode()) {
            case "BTC_LEGACY":
            case "BTC_TESTNET_LEGACY": {
                this.tag = "LEGACY";
                break;
            }
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_NATIVE_SEGWIT": {
                this.tag = "Native Segwit";
                break;
            }
            default: {
                this.tag = "Nested Segwit";
            }
        }

        switch (entity.getCoinCode()) {
            case "BTC_LEGACY":
            case "BTC_TESTNET_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_NATIVE_SEGWIT":
            case "BTC":
            case "BTC_TESTNET_SEGWIT": {
                this.hasTag = true;
                break;
            }
            default: {
                this.hasTag = false;
            }
        }
    }

    public String getTag() {
        return this.tag;
    }

    public boolean hasTag() {
        return this.hasTag;
    }

    public long getId() {
        return id;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getCoinId() {
        return coinId;
    }

    public String getName() {
        return name;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public String getDisplayCoinCode() {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
                return "BTC";
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
                return "XTN";
        }
        return displayCoinCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(int addressCount) {
        this.addressCount = addressCount;
    }

    public boolean isShow() {
        return isShow;
    }

    public String getExPub() {
        return exPub;
    }

    public int getIndex() {
        return index;
    }
}
