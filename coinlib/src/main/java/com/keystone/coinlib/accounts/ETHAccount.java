package com.keystone.coinlib.accounts;

import java.util.regex.Pattern;

public enum ETHAccount {
    LEDGER_LIVE("M/44'/60'", 0x3c, "Ledger Live", "ledger_live", "m/44'/60'/*'/0/0"),
    LEDGER_LEGACY("M/44'/60'/0'", 0x3c, "Ledger Legacy", "ledger_legacy", "m/44'/60'/0'/*"),
    BIP44_STANDARD("M/44'/60'/0'", 0x3c, "BIP44 Standard", "standard", "m/44'/60'/0'/0/*");

    private String path;
    private int type;
    private String name;
    private String code;
    private String displayPath;

    ETHAccount(String path, int type, String name, String code, String displayPath) {
        this.path = path;
        this.type = type;
        this.name = name;
        this.code = code;
        this.displayPath = displayPath;
    }

    public static ETHAccount ofCode(String code) {
        if (code.equals(LEDGER_LIVE.code)) return LEDGER_LIVE;
        if (code.equals(LEDGER_LEGACY.code)) return LEDGER_LEGACY;
        if (code.equals(BIP44_STANDARD.code)) return BIP44_STANDARD;
        throw new RuntimeException("invalid eth account code: " + code);
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

    public String getCode() {
        return code;
    }

    public static boolean isLedgerLiveChildren(String path) {
        return Pattern.matches("^M/44'/60'/\\d+'/0/0", path);
    }

    public static boolean isLedgerLegacyChildren(String path) {
        return Pattern.matches("^M/44'/60'/0'/\\d+", path);
    }

    public static boolean isStandardChildren(String path) {
        return Pattern.matches("^M/44'/60'/0'/0/\\d+", path);
    }

    public boolean isChildrenPath(String path) {
        if (!path.toUpperCase().startsWith("M/")) {
            path = "M/" + path;
        }
        switch (this) {
            case LEDGER_LIVE:
                return isLedgerLiveChildren(path);
            case LEDGER_LEGACY:
                return isLedgerLegacyChildren(path);
            default:
                return isStandardChildren(path);
        }
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public static ETHAccount getAccountByPath(String path) {
        if (!path.toUpperCase().startsWith("M/")) {
            path = "M/" + path;
        }
        if (isStandardChildren(path)) return BIP44_STANDARD;
        if (isLedgerLegacyChildren(path)) return LEDGER_LEGACY;
        if (isLedgerLiveChildren(path)) return LEDGER_LIVE;
        return null;
    }
}
