package com.keystone.cold.util;

import android.os.SystemProperties;

import org.spongycastle.util.encoders.Hex;


public class DeviceInfoUtil {

    private static final String PREFIX = "keystone";

    public static String getDeviceType() {
        String boardType = SystemProperties.get("boardtype");
        if ("B".equals(boardType)) {
            return "Keystone Essential";
        } else {
            return "Keystone Pro";
        }
    }

    public static String getDeviceId() {
        String serial = android.os.Build.getSerial();
        byte[] hashResult = HashUtil.twiceSha256((PREFIX + serial));
        if (hashResult != null) {
            return Hex.toHexString(hashResult).substring(0, 40);
        }
        return null;
    }
}
