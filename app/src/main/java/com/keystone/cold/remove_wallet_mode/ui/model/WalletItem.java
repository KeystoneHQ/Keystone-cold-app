package com.keystone.cold.remove_wallet_mode.ui.model;

import com.keystone.cold.db.entity.FilterableItem;

public class WalletItem implements FilterableItem {

    private final String walletId;
    private final String walletName;
    private final String walletSummary;

    public WalletItem(String walletId, String walletName, String walletSummary) {
        this.walletId = walletId;
        this.walletName = walletName;
        this.walletSummary = walletSummary;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }


    public String getWalletSummary() {
        return walletSummary;
    }


    @Override
    public String toString() {
        return "WalletItem{" +
                "walletCode='" + walletId + '\'' +
                ", walletName='" + walletName + '\'' +
                ", walletSummary='" + walletSummary + '\'' +
                '}';
    }

    @Override
    public boolean filter(String s) {
        return false;
    }
}
