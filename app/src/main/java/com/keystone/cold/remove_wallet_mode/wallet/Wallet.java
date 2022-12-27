package com.keystone.cold.remove_wallet_mode.wallet;

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
    FEWCHA("fewcha"),
    SENDER("sender"),
    RABBY("rabby"),
    CORE("core"),
    POLKADOT("polkadot"),
    XRPTOOLKIT("xrp"),
    ZAPPER("zapper"),
    YEARN("yearn"),
    SUSHISWAP("sushiswap");


    private final String walletId;

    Wallet(String walletId) {
        this.walletId = walletId;
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
}

