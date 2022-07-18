package com.keystone.coinlib.accounts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public enum NEARAccount {

    // m / purpose' / coin_type' / account' / change / address_index
    MNEMONIC("M/44'/397'/0'", "Standard", "near_mnemonic", "M/44'/397'/0'"),
    LEDGER("M/44'/397'/0'/0'/0'","Ledger Live","near_ledger","M/44'/397'/0'/0'/*'");


    private String path;
    private String name;
    private String code;
    private String displayPath;

    NEARAccount(String path, String name, String code, String displayPath) {
        this.path = path;
        this.name = name;
        this.code = code;
        this.displayPath = displayPath;
    }

    public static NEARAccount ofAddition(String addition) {
        String code = null;
        try {
            code = new JSONObject(addition).getString("near_account");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return ofCode(code);
    }

    public static NEARAccount ofCode(String code) {
        if (code == null) {
            return MNEMONIC;
        }
        switch (code) {
            case "near_mnemonic":
                return MNEMONIC;
            case "near_ledger":
                return LEDGER;
        }
        return MNEMONIC;
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
        String path = MNEMONIC.getPath();
        switch (code) {
            case "near_mnemonic":
                path = MNEMONIC.getPath();
                break;
            case "near_ledger":
                path = LEDGER.getPath();
                break;
        }
        return path;
    }

    public boolean isChildrenPath(String path) {
        if (!path.toUpperCase().startsWith("M/")) {
            path = "M/" + path;
        }
        switch (this) {
            case MNEMONIC:
                return isMnemonic(path);
            case LEDGER:
                return isLedger(path);
        }

        return false;
    }


    public static boolean isMnemonic(String path) {
        return Pattern.matches("^M/44'/397'/0'", path);
    }

    public static boolean isLedger(String path) {
        return Pattern.matches("^M/44'/397'/0'/0'/\\d+'", path);
    }
}
