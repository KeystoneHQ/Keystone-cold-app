package com.keystone.coinlib.accounts;

public enum Chains {
    ETH("m/44'/60'/0'", 0x3c);

    private String path;
    private int type;

    Chains(String path, int type) {
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
