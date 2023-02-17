package com.keystone.cold.util;

import android.text.TextUtils;

/**
 * utf-8编码规则：
 * utf-8是一种可变长编码， 使用1——4个字节表示一个字符。
 * 1、对于单字节字符，字节第一位置0， 后面7位为该字符的Unicode码。Unicode兼容ASCII码，对于单字节字符，二者相同。
 * 2、对于n字节字符（n>1, n<=4), 第一字节的前n位置1， 第n+1位置0，其余字节前两位置10.剩下未提及的二进制位，置为该字符的Unicode码。
 * <p>
 * 基于以上规则，实现utf-8格式校验功能
 */

public class CharSetUtil {

    public static boolean isUTF8Format(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        int size = data.length;
        int i = 0;
        while (i < size) {
            CharMode mode = whichCharMode(data[i]);
            switch (mode) {
                case NOT_A_CHAR:
                    return false;
                case ONE_BYTE:
                    i++;
                    break;
                case TOW_BYTES:
                    if (isInvalidMultiBytesData(data, i + 1, i + 2)) {
                        return false;
                    }
                    i += 2;
                    break;
                case THREE_BYTES:
                    if (isInvalidMultiBytesData(data, i + 1, i + 3)) {
                        return false;
                    }
                    i += 3;
                    break;
                case FOUR_BYTES:
                    if (isInvalidMultiBytesData(data, i + 1, i + 4)) {
                        return false;
                    }
                    i += 4;
                    break;
            }
        }
        return true;
    }

    enum CharMode {
        NOT_A_CHAR, ONE_BYTE, TOW_BYTES, THREE_BYTES, FOUR_BYTES
    }

    private static CharMode whichCharMode(byte initialByte) {
        CharMode charMode = CharMode.NOT_A_CHAR;
        int value = initialByte & 0xff;
        if (value < 0x80) {
            charMode = CharMode.ONE_BYTE;
        } else if (value < 0xC0) {
            charMode = CharMode.NOT_A_CHAR;
        } else if (value < 0xE0) {
            charMode = CharMode.TOW_BYTES;
        } else if (value < 0xF0) {
            charMode = CharMode.THREE_BYTES;
        } else if (value < 0xF8) {
            charMode = CharMode.FOUR_BYTES;
        }
        return charMode;
    }

    private static boolean isInvalidMultiBytesData(byte[] values, int start, int end) {
        if (end > values.length) {
            return true;
        }
        for (int i = start; i < end; i++) {
            if (isInvalidData(values[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInvalidData(byte value) {
        return (value & (0xC0)) != 0x80;
    }


    public static boolean isHexString(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        str = str.replace("0x", "");
        str = str.replace("0X", "");
        String regex = "^[A-Fa-f0-9]+$";
        if (str.matches(regex)) {
            return true;
        }
        return false;
    }
}
