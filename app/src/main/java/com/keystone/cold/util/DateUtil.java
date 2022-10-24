package com.keystone.cold.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yyyy H:m:s", Locale.UK);
        return simpleDateFormat.format(date);
    }
}
