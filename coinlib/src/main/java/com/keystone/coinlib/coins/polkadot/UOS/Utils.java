package com.keystone.coinlib.coins.polkadot.UOS;

public  class Utils {
    public static int tryParseInt(String intHexStr) {
        try {
            return Integer.parseInt(intHexStr, 16);
        } catch (Exception e) {
            return 0;
        }
    }
}