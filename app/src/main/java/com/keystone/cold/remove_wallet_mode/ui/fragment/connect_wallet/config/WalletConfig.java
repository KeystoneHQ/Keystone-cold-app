package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.config;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum WalletConfig {
    METAMASK(Wallet.METAMASK.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    RABBY(Wallet.RABBY.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    SAFE(Wallet.SAFE.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    ZAPPER(Wallet.ZAPPER.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    YEARN(Wallet.YEARN.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    SUSHISWAP(Wallet.SUSHISWAP.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true, false),
    FEWCHA(Wallet.FEWCHA.getWalletId(), new String[]{Coins.SUI.coinId(), Coins.APTOS.coinId()}, false, true, true, true),
    SUIET(Wallet.SUIET.getWalletId(), new String[]{Coins.SUI.coinId()}, false, true, true, false),
    PETRA(Wallet.PETRA.getWalletId(), new String[]{Coins.APTOS.coinId()}, false, true, true, false),
    SOLFLARE(Wallet.SOLFLARE.getWalletId(), new String[]{Coins.SOL.coinId()}, true, true, true, false),
    SENDER(Wallet.SENDER.getWalletId(), new String[]{Coins.NEAR.coinId()}, true, false, true, false),
    POLKADOT(Wallet.POLKADOTJS.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true, false),
    SUBWALLET(Wallet.SUBWALLET.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true, false),
    XRPToolkit(Wallet.XRPTOOLKIT.getWalletId(), new String[]{Coins.XRP.coinId()}, false, true, true, false),
    CORE(Wallet.CORE.getWalletId(), new String[]{Coins.BTC.coinId(), Coins.ETH.coinId()}, false, false, true, false),
    BITKEEP(Wallet.BITKEEP.getWalletId(), new String[]{Coins.BTC.coinId(), Coins.ETH.coinId()}, true, false, true, false),
    BITKEEP_ONLY_BTC(Wallet.BITKEEP.getWalletId(), new String[]{Coins.BTC.coinId()}, false, false, true, false),
    KEYSTONE(Wallet.KEYSTONE.getWalletId(), new String[]{Coins.BTC.coinId(), Coins.ETH.coinId()}, false, false, true, true),
    ETERNL(Wallet.ETERNL.getWalletId(), new String[]{Coins.ADA.coinId()}, false, false, true, false),
    OKX(Wallet.OKX.getWalletId(), new String[]{Coins.BTC.coinId(), Coins.ETH.coinId()}, false, false, true, true),
    DEFAULT("default", new String[]{""}, false, false, true, true),
    ;

    private static final List<String> OPEN_COINS = Arrays.asList(Coins.BTC.coinId(), Coins.ETH.coinId());

    public static void setOpenCoins(List<String> openCoins) {
        OPEN_COINS.clear();
        OPEN_COINS.addAll(openCoins);
    }

    private String walletId;

    public String[] getCoinIds() {
        return coinIds;
    }

    private String[] coinIds;
    private boolean showChangePath;
    private boolean showSelectAddress;
    private boolean showTutorial;
    private boolean networkCheckable;

    WalletConfig(String walletId, String[] coinIds, boolean showChangePath, boolean showSelectAddress, boolean showTutorial, boolean networkCheckable) {
        this.walletId = walletId;
        this.coinIds = coinIds;
        this.showChangePath = showChangePath;
        this.showSelectAddress = showSelectAddress;
        this.showTutorial = showTutorial;
        this.networkCheckable = networkCheckable;
    }

    public static WalletConfig getConfigByWalletId(String walletId) {
        Optional<WalletConfig> config = Arrays.stream(WalletConfig.values()).filter(assetConfig -> assetConfig.walletId.equals(walletId)).findFirst();
        if (config.isPresent()) {
            if (config.get() == WalletConfig.BITKEEP) {
                if (!OPEN_COINS.contains(Coins.ETH.coinId())) {
                    return BITKEEP_ONLY_BTC;
                }
            }
            return config.get();
        }
        return DEFAULT;
    }


    public String getWalletId() {
        return walletId;
    }

    public boolean isShowChangePath() {
        return showChangePath;
    }

    public boolean isShowSelectAddress() {
        return showSelectAddress;
    }

    public boolean isShowTutorial() {
        return showTutorial;
    }

    public String getCoinId() {
        // should determine which coinId to use when it is a multi_chain wallet;
        if (this == WalletConfig.BITKEEP) {
            return coinIds[1];
        }
        return coinIds[0];

    }

    public boolean isNetworkCheckable() {
        return networkCheckable;
    }
}
