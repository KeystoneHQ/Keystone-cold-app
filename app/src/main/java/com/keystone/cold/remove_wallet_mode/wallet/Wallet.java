package com.keystone.cold.remove_wallet_mode.wallet;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.config.FeatureFlags;

public enum Wallet {

    KEYSTONE("keystone"),
    METAMASK("metamask"),
    KEPLR("keplr"),
    SAFE("safe"),
    BITKEEP("bitkeep"),
    BLUEWALLET("bluewallet"),
    SOLFLARE("solflare"),
    SUBWALLET("subwallet"),
    ETERNL("eternl"),
    ARCONNECT("arconnect"),
    PETRA("petra"),
    FEWCHA("fewcha", "Fewcha"),
    SENDER("sender"),
    RABBY("rabby"),
    CORE("core"),
    POLKADOT("polkadot"),
    XRPTOOLKIT("xrp"),
    ZAPPER("zapper"),
    YEARN("yearn"),
    SUSHISWAP("sushiswap");


    private final String walletId;
    private final String walletName;

    Wallet(String walletId) {
        this.walletId = walletId;
        this.walletName = "";
    }

    Wallet(String walletId, String walletName) {
        this.walletId = walletId;
        this.walletName = walletName;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }

    public static Wallet getWalletById(String walletId) {
        Wallet selectWatchWallet = KEYSTONE;
        for (Wallet wallet : Wallet.values()) {
            if (wallet.walletId.equals(walletId)) {
                selectWatchWallet = wallet;
                break;
            }
        }
        return selectWatchWallet;
    }

    public static boolean isSingleChainWallet(String walletId) {
        Wallet selectWatchWallet = KEYSTONE;
        for (Wallet wallet : Wallet.values()) {
            if (wallet.walletId.equals(walletId)) {
                selectWatchWallet = wallet;
                break;
            }
        }
        return isSingleChainWallet(selectWatchWallet);
    }


    public static boolean isSingleChainWallet(Wallet wallet) {
        //todo  Need to add other types
        switch (wallet) {
            case FEWCHA:
                return true;
            case CORE:
                return false;
        }
        return false;
    }


    public Coins.Coin[] getSupportedCoins() {
        switch (this) {
            case KEYSTONE:
                if (FeatureFlags.ENABLE_XTN) {
                    return new Coins.Coin[]{Coins.BTC, Coins.BTC_LEGACY, Coins.BTC_NATIVE_SEGWIT, Coins.BCH, Coins.ETH, Coins.XRP, Coins.TRON, Coins.LTC, Coins.DASH, Coins.DOT, Coins.BTC_TESTNET_LEGACY, Coins.BTC_TESTNET_SEGWIT, Coins.BTC_TESTNET_NATIVE_SEGWIT};
                } else {
                    return new Coins.Coin[]{Coins.BTC, Coins.BTC_LEGACY, Coins.BTC_NATIVE_SEGWIT, Coins.BCH, Coins.ETH, Coins.XRP, Coins.TRON, Coins.LTC, Coins.DASH, Coins.DOT};
                }
            case POLKADOT:
                return new Coins.Coin[]{Coins.DOT, Coins.KSM};
            case XRPTOOLKIT:
                return new Coins.Coin[]{Coins.XRP};
            case METAMASK:
                return new Coins.Coin[]{Coins.ETH};
            case SOLFLARE:
                return new Coins.Coin[]{Coins.SOL};
            case SENDER:
                return new Coins.Coin[]{Coins.NEAR};
            case CORE:
                return new Coins.Coin[]{Coins.BTC_NATIVE_SEGWIT, Coins.ETH};
            case BITKEEP:
                return new Coins.Coin[]{Coins.BTC_NATIVE_SEGWIT, Coins.ETH};
            case FEWCHA:
                return new Coins.Coin[]{Coins.APTOS};
            case KEPLR:
                return new Coins.Coin[]{Coins.ATOM, Coins.OSMO, Coins.SCRT, Coins.AKT, Coins.CRO, Coins.IOV, Coins.ROWAN, Coins.CTK, Coins.IRIS, Coins.REGEN, Coins.XPRT, Coins.DVPN, Coins.IXO, Coins.NGM, Coins.BLD, Coins.BOOT, Coins.JUNO, Coins.STARS, Coins.AXL, Coins.SOMM, Coins.UMEE, Coins.GRAV, Coins.TGD, Coins.STRD, Coins.KAVA, Coins.EVMOS};
        }
        return new Coins.Coin[]{};
    }
}

