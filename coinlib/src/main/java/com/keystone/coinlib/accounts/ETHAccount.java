package com.keystone.coinlib.accounts;

public enum ETHAccount {
    LEDGER_LIVE("M/44'/60'", 0x3c, "Ledger Live"),
    LEDGER_LEGACY("M/44'/60'/0'", 0x3c, "Ledger Legacy"),
    BIP44_STANDARD("M/44'/60'/0'/0", 0x3c, "BIP44 Standard");

    private String path;
    private int type;
    private String name;

    ETHAccount(String path, int type, String name) {
        this.path = path;
        this.type = type;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
