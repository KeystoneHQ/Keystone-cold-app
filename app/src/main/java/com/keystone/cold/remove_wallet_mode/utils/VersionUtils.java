package com.keystone.cold.remove_wallet_mode.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.keystone.cold.MainApplication;

public class VersionUtils {

    public static String getVersion() {

        Context context = MainApplication.getApplication();
        String version = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (version != null && version.length() > 2) {
            version = "M-" + version.substring(2);
        }

        return version;
    }
}
