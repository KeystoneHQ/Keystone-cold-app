package com.keystone.coinlib.accounts;

import java.util.regex.Pattern;

public enum SOLAccount {


    // 规范的派生路径
    // m / purpose' / coin_type' / account' / change / address_index

    SOLFLARE_BIP44_ROOT("M/44'/501'", "Solflare Bip44 Root", "solfare_bip44_root", "m/44'/501'"), //只对应一个地址
    SOLFLARE_BIP44("M/44'/501'/0'", "Solflare Bip44", "solflare_bip44", "m/44'/501'/*'"),//account 变化
    SOLFLARE_BIP44_CHANGE("M/44'/501'/0'/0'", "SolFlare Bip44 Change", "solflare_bip44_change", "m/44'/501'/*'/0'"); //account 变化

//    SOLFLARE_BIP44_LEDGER_ROOT("M/44'/501'", "Solflare Bip44 Ledger Root", "solflare_bip44_ledger_root", "m/44'/501'/*'"),//先root 然后derived一级  M/44'/501'/0' account 变化
//    SOLFLARE_BIP44_LEDGER("M/44'/501'/0'", "Solflare Bip44 Ledger", "solflare_bip44_ledger", "m/44'/501/0'/*'"), //先root  然后derived一级 M/44'/501'/0'/{account}' account 变化
//    SOLFLARE_BIP44_LEDGER_CHANGE("M/44'/501'/0'/0'", "Solflare Bip44 Ledger Change", "solflare_bip44_ledger_change", "m/44'/501'/0'/0'/*'"), //先root M/44'/501'/0'/0'/{account}‘   增加两级   account变化
////
//    PHANTOM_BIP44_CHANGE("M/44'/501'/0'/0'", "Phantom Bip44 Change", "phantom_bip44_change", "m/44'/501'/0'/*'"), // change 变化
//    PHANTOM_BIP44_LEDGER_ROOT("M/44'/501'", "Phantom Bip44 Ledger Root", "phantom_bip44_ledger_root", "m/44'/501'"), //只对应一个地址
//    PHANTOM_BIP44_LEDGER_ACCOUNT("M/44'/501'/0'", "Phantom Bip44 Ledger Account", "phantom_bip44_ledger_account", "m/44'/501'/*'"), //account 变化
//    PHANTOM_BIP44_LEDGER_CHANGE("M/44'/501'/0'/0'", "Phantom Bip44 Ledger Change", "phantom_bip44_ledger_change", "m/44'/501'/*'/0'");//M/44'/501'/{account}'/0' account 变化


    private String path;
    private String name;
    private String code;
    private String displayPath;

    SOLAccount(String path, String name, String code, String displayPath) {
        this.path = path;
        this.name = name;
        this.code = code;
        this.displayPath = displayPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
    }


    public static String getPathByCode(String code) {
        String path = SOLFLARE_BIP44_ROOT.getPath();
        switch (code) {
            case "solfare_bip44_root":
                path = SOLFLARE_BIP44_ROOT.getPath();
                break;
            case "solflare_bip44":
                path = SOLFLARE_BIP44.getPath();
                break;
            case "solflare_bip44_change":
                path = SOLFLARE_BIP44_CHANGE.getPath();
                break;
//            case "solflare_bip44_ledger_root":
//                path = SOLFLARE_BIP44_LEDGER_ROOT.getPath();
//                break;
//            case "solflare_bip44_ledger":
//                path = SOLFLARE_BIP44_LEDGER.getPath();
//                break;
//            case "solflare_bip44_ledger_change":
//                path = SOLFLARE_BIP44_LEDGER_CHANGE.getPath();
//                break;
//            case "phantom_bip44_change":
//                path = PHANTOM_BIP44_CHANGE.getPath();
//                break;
//            case "phantom_bip44_ledger_root":
//                path = PHANTOM_BIP44_LEDGER_ROOT.getPath();
//                break;
//            case "phantom_bip44_ledger_account":
//                path = PHANTOM_BIP44_LEDGER_ACCOUNT.getPath();
//                break;
//            case "phantom_bip44_ledger_change":
//                path = PHANTOM_BIP44_LEDGER_CHANGE.getPath();
//                break;

        }
        return path;
    }


    public static SOLAccount ofCode(String code){
        if (code == null) {
            return SOLFLARE_BIP44_ROOT;
        }
        switch (code) {
            case "solfare_bip44_root" :
                return SOLFLARE_BIP44_ROOT;
            case "solflare_bip44":
                return SOLFLARE_BIP44;
            case "solflare_bip44_change":
                return SOLFLARE_BIP44_CHANGE;
        }
        return SOLFLARE_BIP44_ROOT;
    }

    public static boolean isLedgerRoot(String path) {
        return Pattern.matches("^M/44'/501'", path);
    }

    public static boolean isLedgerBip44(String path) {
        return Pattern.matches("^M/44'/501'/\\d+'", path);
    }

    public static boolean isLedgerBip44Change(String path) {
        return Pattern.matches("^M/44'/501'/\\d+'/0'", path);
    }


    public boolean isChildrenPath(String path) {
        if (!path.toUpperCase().startsWith("M/")) {
            path = "M/" + path;
        }
        switch (this){
            case SOLFLARE_BIP44_ROOT:
                return isLedgerRoot(path);
            case SOLFLARE_BIP44:
                return isLedgerBip44(path);
            default:
                return isLedgerBip44Change(path);
        }
    }
}
