package com.keystone.coinlib.accounts;

import com.keystone.coinlib.utils.Coins;

public enum BTCAccount {
    NATIVE_SEGWIT("M/84'/0'/0'", "Native Segwit", "native_segwit", "m/84'/0'/0'/0/*", Coins.BTC_NATIVE_SEGWIT.coinId()),
    NESTED_SEGWIT("M/49'/0'/0'", "Nested Segwit", "nested_legacy", "m/49'/0'/0'/0/*", Coins.BTC.coinId()),
    LEGACY("M/44'/0'/0'", "Legacy", "legacy", "m/44'/0'/0'/0/*", Coins.BTC_LEGACY.coinId()),
    CORE_NATIVE_SEGWIT("M/44'/60'/0'", "Core Native Segwit", "core_native_segwit", "m/44'/60'/0'/0/*", Coins.BTC_CORE_WALLET.coinId());

    private String path;
    private String name;
    private String code;
    private String displayPath;
    private String coinId;

    BTCAccount(String path, String name, String code, String displayPath, String coinId) {
        this.path = path;
        this.name = name;
        this.code = code;
        this.displayPath = displayPath;
        this.coinId = coinId;
    }

    public static BTCAccount ofCode(String code) {
        if (code.equals(LEGACY.code)) return LEGACY;
        if (code.equals(NESTED_SEGWIT.code)) return NESTED_SEGWIT;
        if (code.equals(NATIVE_SEGWIT.code)) return NATIVE_SEGWIT;
        if (code.equals(CORE_NATIVE_SEGWIT.code)) return CORE_NATIVE_SEGWIT;
        throw new RuntimeException("invalid btc account code: " + code);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public String getCoinId() {
        return coinId;
    }
}
