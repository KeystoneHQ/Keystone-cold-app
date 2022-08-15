package com.keystone.cold.util;

public class StringUtils {
    public static String capitalizes(String str) {
        char[] c = str.toCharArray();
        if(c[0] >= 97 && c[0] <= 122){
            c[0] -= 32;
        }
        return String.valueOf(c);
    }
}
