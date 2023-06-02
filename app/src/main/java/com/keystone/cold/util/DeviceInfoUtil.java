package com.keystone.cold.util;

import android.os.SystemProperties;

public class DeviceInfoUtil {

    public static String getDeviceType() {
        String boardType = SystemProperties.get("boardtype");
        if ("B".equals(boardType)) {
            return "keystone Essential";
        } else {
            return "keystone Pro";
        }
    }

    public static String getDeviceSerial() {
        return android.os.Build.getSerial();
    }
}
