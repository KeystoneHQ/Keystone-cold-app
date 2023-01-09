package com.keystone.cold.remove_wallet_mode.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferencesUtil {

    public static final String PREFERENCE_SECRET = "secret";

    public static final String COIN_CONFIG_VERSION_KEY = "coin_config_version_key";
    public static final String COIN_CONFIG_KEY = "coin_config_key";

    public static final String FIRMWARE_VERSION_KEY = "firmware_version_key";


    public static int getCoinConfigVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getInt(COIN_CONFIG_VERSION_KEY,0);
    }

    public static void setCoinConfigVersion(Context context, int version) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putInt(COIN_CONFIG_VERSION_KEY, version).apply();
    }

    public static String getCoinConfig(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(COIN_CONFIG_KEY,"");
    }

    public static void setCoinConfig(Context context, String config) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(COIN_CONFIG_KEY, config).apply();
    }


    public static String getFirmwareVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(FIRMWARE_VERSION_KEY,"");

    }

    public static void setFirmwareVersion(Context context, String firmwareVersion) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(FIRMWARE_VERSION_KEY, firmwareVersion).apply();
    }

}
