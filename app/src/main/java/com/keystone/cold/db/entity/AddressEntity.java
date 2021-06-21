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
import androidx.room.PrimaryKey;

import com.keystone.cold.model.Address;

@Entity(tableName = "addresses")

public class AddressEntity implements Address, FilterableItem {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String path;
    private String coinId;
    private String addressString;
    private String name;
    private int index;
    private String belongTo;

    public AddressEntity() {
    }

    public AddressEntity(Address address) {
        id = address.getId();
        belongTo = address.getBelongTo();
        path = address.getPath();
        coinId = address.getCoinId();
        addressString = address.getAddressString();
        name = address.getName();
        index = address.getIndex();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    @Override
    public String getBelongTo() {
        return belongTo;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getAddressString() {
        return addressString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getCoinId() {
        return coinId;
    }

    @NonNull
    @Override
    public String toString() {
        return "AddressEntity{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", coinId='" + coinId + '\'' +
                ", addressString='" + addressString + '\'' +
                ", name='" + name + '\'' +
                ", index=" + index +
                ", belongTo='" + belongTo + '\'' +
                '}';
    }

    @Override
    public boolean filter(String s) {

        if (TextUtils.isEmpty(s)) {
            return true;
        }
        s = s.toLowerCase();
        return name.toLowerCase().contains(s)
                || addressString.toLowerCase().contains(s);
    }
}
