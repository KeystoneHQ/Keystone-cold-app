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
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.keystone.cold.db.viewmodel.CoinModel;
import com.keystone.cold.model.Coin;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "coins", indices = {@Index("id")})
public class CoinEntity implements Coin, FilterableItem {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String coinId;
    private String name;
    private int iconResId;
    private boolean show = false;
    private int addressCount;
    private String coinCode;
    private String exPub;
    private String belongTo;
    private int index;
    private String addition;

    @Ignore
    private final List<AccountEntity> accounts = new ArrayList<>();

    public CoinEntity() {
    }

    public CoinEntity(@NonNull Coin coin) {
        id = coin.getId();
        coinId = coin.getCoinId();
        name = coin.getName();
        iconResId = getIconResId();
        show = coin.isShow();
        addressCount = coin.getAddressCount();
        coinCode = coin.getCoinCode();
        belongTo = coin.getBelongTo();
        exPub = coin.getExPub();
        index = coin.getIndex();
    }

    public CoinEntity(@NonNull CoinModel coin) {
        id = coin.getId();
        coinId = coin.getCoinId();
        name = coin.getName();
        iconResId = getIconResId();
        show = coin.isShow();
        addressCount = coin.getAddressCount();
        coinCode = coin.getCoinCode();
        belongTo = coin.getBelongTo();
        exPub = coin.getExPub();
        index = coin.getIndex();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCoinCode() {
        return coinCode;
    }

    @Override
    public String getDisplayName() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    @Override
    public int getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(int addressCount) {
        this.addressCount = addressCount;
    }

    @Override
    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    @Override
    public String getExPub() {
        return exPub;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    @Override
    public String getBelongTo() {
        return belongTo;
    }

    public void setExPub(String exPub) {
        this.exPub = exPub;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public void addAccount(AccountEntity account) {
        accounts.add(account);
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAddition(String json){
        this.addition = json;
    }

    public String getAddition(){
        return this.addition;
    }

    @NonNull
    @Override
    public String toString() {
        return "CoinEntity{" +
                "id=" + id +
                ", coinId='" + coinId + '\'' +
                ", name='" + name + '\'' +
                ", iconResId=" + iconResId +
                ", show=" + show +
                ", addressCount=" + addressCount +
                ", coinCode='" + coinCode + '\'' +
                ", exPub='" + exPub + '\'' +
                ", belongTo='" + belongTo + '\'' +
                '}';
    }

    @Override
    public boolean filter(String s) {
        if (TextUtils.isEmpty(s)) {
            return true;
        }
        s = s.toLowerCase();
        return name.toLowerCase().contains(s) || coinCode.toLowerCase().contains(s);
    }

    public CoinModel toCoinModel() {
        return new CoinModel(this);
    }
}
