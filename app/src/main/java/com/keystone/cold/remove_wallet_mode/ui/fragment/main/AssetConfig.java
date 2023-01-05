package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import com.keystone.coinlib.utils.Coins;

import java.util.Arrays;
import java.util.Optional;

public enum AssetConfig {
    ETH(Coins.ETH.coinId(), true, true, true),
    DEFAULT("default", true, true, true),
    ;

    private String coinId;
    private boolean showAddAddress;
    private boolean showChangePath;
    private boolean showFAQ;

    AssetConfig(String coinId, boolean showAddAddress, boolean showChangePath, boolean showFAQ) {
        this.coinId = coinId;
        this.showAddAddress = showAddAddress;
        this.showChangePath = showChangePath;
        this.showFAQ = showFAQ;
    }

    public static AssetConfig getConfigByCoinId(String coinId) {
        Optional<AssetConfig> config = Arrays.stream(AssetConfig.values()).filter(assetConfig -> assetConfig.coinId.equals(coinId)).findFirst();
        return config.orElse(DEFAULT);
    }

    public boolean isShowAddAddress() {
        return showAddAddress;
    }

    public boolean isShowChangePath() {
        return showChangePath;
    }

    public boolean isShowFAQ() {
        return showFAQ;
    }
}
