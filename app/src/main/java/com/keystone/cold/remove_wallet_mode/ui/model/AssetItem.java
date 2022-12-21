package com.keystone.cold.remove_wallet_mode.ui.model;

import static com.keystone.cold.remove_wallet_mode.ui.helper.CoinConfigHelper.getCoinEco;

import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.FilterableItem;

import java.util.List;

public class AssetItem implements FilterableItem {

    private String coinId;
    private String coinCode;
    private String network;
    private List<String> ecology;

    private boolean show;


    public AssetItem() {
    }

    public AssetItem(CoinEntity coinEntity) {
        this.coinId = coinEntity.getCoinId();
        this.coinCode = coinEntity.getCoinCode();
        this.network = coinEntity.getName();
        this.ecology = getCoinEco(coinEntity.getCoinCode());
        this.show = coinEntity.isShow();
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public List<String> getEcology() {
        return ecology;
    }

    public void setEcology(List<String> ecology) {
        this.ecology = ecology;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }


    @Override
    public String toString() {
        return "AssetItem{" +
                "coinId='" + coinId + '\'' +
                ", coinCode='" + coinCode + '\'' +
                ", network='" + network + '\'' +
                ", ecology=" + ecology +
                ", show=" + show +
                '}';
    }

    @Override
    public boolean filter(String s) {
        return false;
    }
}
