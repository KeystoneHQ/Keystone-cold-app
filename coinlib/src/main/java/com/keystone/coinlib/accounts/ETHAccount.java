package com.keystone.coinlib.accounts;

public enum ETHAccount {
    LEDGER_LIVE("M/44'/60'", 0x3c),
    LEGACY("M/44'/60'/0'", 0x3c),
    BIP44_STANDARD("M/44'/60'/0'/0", 0x3c);

    private String path;
    private int type;

    ETHAccount(String path, int type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return type;
    }
}
