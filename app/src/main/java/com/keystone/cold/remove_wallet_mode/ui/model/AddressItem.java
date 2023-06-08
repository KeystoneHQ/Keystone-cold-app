package com.keystone.cold.remove_wallet_mode.ui.model;

import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.FilterableItem;

public class AddressItem implements FilterableItem {


    private final long id;
    private final String coinId;
    private String name;
    private final String address;
    private final String path;
    private final String displayPath;
    private final int index;


    public AddressItem(AddressEntity addressEntity) {
        this.id = addressEntity.getId();
        this.coinId = addressEntity.getCoinId();
        this.name = addressEntity.getName();
        this.address = addressEntity.getAddressString();
        this.displayPath = addressEntity.getDisplayPath();
        this.path = addressEntity.getPath();
        this.index = addressEntity.getIndex();
    }

    public long getId() {
        return id;
    }

    public String getCoinId() {
        return coinId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public String getPath() {
        return path;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean filter(String s) {
        return false;
    }

    @Override
    public String toString() {
        return "AddressItem{" +
                "id=" + id +
                ", coinId='" + coinId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", path='" + path + '\'' +
                ", displayPath='" + displayPath + '\'' +
                '}';
    }
}
