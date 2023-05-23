package com.keystone.cold.remove_wallet_mode.wallet;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.config.FeatureFlags;

public enum Wallet {

    KEYSTONE("keystone", "Keystone App"),
    METAMASK("metamask", "MetaMask"),
    OKX("okx", "OKX Wallet"),
    KEPLR("keplr", "Keplr"),
    SAFE("safe", "Safe"),
    BITKEEP("bitkeep", "BitKeep"),
    BLUEWALLET("bluewallet", "BlueWallet"),
    SOLFLARE("solflare", "Solflare"),
    SUBWALLET("subwallet", "SubWallet"),
    ETERNL("eternl", "Eternl Wallet"),
    ARCONNECT("arconnect", "ArConnect"),
    PETRA("petra", "Petra"),
    FEWCHA("fewcha", "Fewcha"),
    SUIET("suiet", "Suiet"),
    SENDER("sender", "Sender"),
    BLOCKWALLET("blockwallet", "BlockWallet"),
    RABBY("rabby", "Rabby"),
    CORE("core", "Core"),
    POLKADOTJS("polkadotjs", "Polkadot.{js}"),
    XRPTOOLKIT("xrp", "XRP Toolkit"),
    ZAPPER("zapper", "Zapper"),
    YEARN("yearn", "Yearn Finance"),
    SUSHISWAP("sushiswap", "SushiSwap"),
    UNKNOWNWALLET("unknownwallet", "Unknown Wallet");

    public static final String XRP_TOOLKIT_SIGN_ID = "xrp_toolkit_sign_id";
    public static final String POLKADOT_JS_SIGN_ID = "polkadot_js_sign_id";
    public static final String METAMASK_SIGN_ID = "metamask_sign_id";
    public static final String SOLANA_SIGN_ID = "solana_sign_id";
    public static final String NEAR_SIGN_ID = "near_sign_id";
    public static final String APTOS_SIGN_ID = "aptos_sign_id";
    public static final String CORE_WALLET_SIGN_ID = "core_wallet_sign_id";
    public static final String BIT_KEEP_SIGN_ID = "bit_keep_sign_id";
    public static final String ARWEAVE_SIGN_ID = "arweave_sign_id";
    public static final String KEPLR_WALLET_SIGN_ID = "keplr_wallet_sign_id";
    public static final String PETRA_WALLET_SIGN_ID = "petra_wallet_sign_id";
    public static final String OKX_WALLET_SIGN_ID = "okx_wallet_sign_id";
    public static final String ETERNL_WALLET_SIGN_ID = "eternl_wallet_sign_id";

    public static final String UNKNOWN_WALLET_SIGN_ID = "unknown_wallet_sign_id";

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

    public String getSignId() {
        switch (this) {
            case POLKADOTJS:
                return POLKADOT_JS_SIGN_ID;
            case XRPTOOLKIT:
                return XRP_TOOLKIT_SIGN_ID;
            case METAMASK:
                return METAMASK_SIGN_ID;
            case SOLFLARE:
                return SOLANA_SIGN_ID;
            case SENDER:
                return NEAR_SIGN_ID;
            case FEWCHA:
                return APTOS_SIGN_ID;
            case PETRA:
                return PETRA_WALLET_SIGN_ID;
            case CORE:
                return CORE_WALLET_SIGN_ID;
            case BITKEEP:
                return BIT_KEEP_SIGN_ID;
            case ARCONNECT:
                return ARWEAVE_SIGN_ID;
            case KEPLR:
                return KEPLR_WALLET_SIGN_ID;
            case ETERNL:
                return ETERNL_WALLET_SIGN_ID;
        }
        return UNKNOWN_WALLET_SIGN_ID;
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

    public static Wallet getWalletByIdOrUnknown(String walletId) {
        Wallet selectWatchWallet = UNKNOWNWALLET;
        for (Wallet wallet : Wallet.values()) {
            if (wallet.walletId.equals(walletId)) {
                selectWatchWallet = wallet;
                break;
            }
        }
        return UNKNOWNWALLET;
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
            case METAMASK:
            case SOLFLARE:
            case BLUEWALLET:
            case SUBWALLET:
            case POLKADOTJS:
            case ARCONNECT:
            case SENDER:
            case ETERNL:
            case SUIET:
                return true;
            case KEPLR:
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
            case POLKADOTJS:
                return new Coins.Coin[]{Coins.DOT, Coins.KSM};
            case XRPTOOLKIT:
                return new Coins.Coin[]{Coins.XRP};
            case METAMASK:
                return new Coins.Coin[]{Coins.ETH};
            case BLUEWALLET:
                return new Coins.Coin[]{Coins.BTC, Coins.BTC_LEGACY, Coins.BTC_NATIVE_SEGWIT};
            case SOLFLARE:
                return new Coins.Coin[]{Coins.SOL};
            case SENDER:
                return new Coins.Coin[]{Coins.NEAR};
            case ETERNL:
                return new Coins.Coin[]{Coins.ADA};
            case CORE:
            case BITKEEP:
                return new Coins.Coin[]{Coins.BTC_NATIVE_SEGWIT, Coins.ETH};
            case FEWCHA:
                return new Coins.Coin[]{Coins.APTOS, Coins.SUI};
            case SUIET:
                return new Coins.Coin[]{Coins.SUI};
            case KEPLR:
                return new Coins.Coin[]{Coins.ATOM, Coins.OSMO, Coins.SCRT, Coins.AKT, Coins.CRO, Coins.IOV, Coins.ROWAN, Coins.CTK, Coins.IRIS, Coins.REGEN, Coins.XPRT, Coins.DVPN, Coins.IXO, Coins.NGM, Coins.BLD, Coins.BOOT, Coins.JUNO, Coins.STARS, Coins.AXL, Coins.SOMM, Coins.UMEE, Coins.GRAV, Coins.TGD, Coins.STRD, Coins.KAVA, Coins.EVMOS};
        }
        return new Coins.Coin[]{};
    }
}
