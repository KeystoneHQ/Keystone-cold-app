/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.encryptioncore.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.encryptioncore.exception.ByteFormatException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class ByteFormatter {
    private static final char[] HEX_ENCODE_TABLE;
    private static final byte[] HEX_DECODE_TABLE;
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]*$");

    static {
        HEX_ENCODE_TABLE = "0123456789abcdef".toCharArray();
        HEX_DECODE_TABLE = new byte[128];

        for (int i = 0, len = HEX_DECODE_TABLE.length; i < len; ++i) {
            HEX_DECODE_TABLE[i] = (byte) 0xF;
        }

        for (int i = 0, len = HEX_ENCODE_TABLE.length; i < len; ++i) {
            HEX_DECODE_TABLE[HEX_ENCODE_TABLE[i]] = (byte) i;
        }

        HEX_DECODE_TABLE['A'] = HEX_DECODE_TABLE['a'];
        HEX_DECODE_TABLE['B'] = HEX_DECODE_TABLE['b'];
        HEX_DECODE_TABLE['C'] = HEX_DECODE_TABLE['c'];
        HEX_DECODE_TABLE['D'] = HEX_DECODE_TABLE['d'];
        HEX_DECODE_TABLE['E'] = HEX_DECODE_TABLE['e'];
        HEX_DECODE_TABLE['F'] = HEX_DECODE_TABLE['f'];
    }

    @NonNull
    public static byte[] short2bytes(int i) {
        return new byte[]{
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    public static int bytes2short(byte[] bytes) {
        if (bytes == null) {
            return 0;
        } else if (bytes.length != 2) {
            throw new ByteFormatException("bytes2short: the length of bytes must be two");
        } else {
            return (bytes[1] & 0xFF)
                    | ((bytes[0] & 0xFF) << 8);
        }
    }

    @NonNull
    public static byte[] int2bytes(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    public static int bytes2int(byte[] bytes) {
        if (bytes == null) {
            return 0;
        } else if (bytes.length != 4) {
            throw new ByteFormatException("bytes2int: the length of bytes must be four");
        } else {
            return (bytes[3] & 0xFF)
                    | ((bytes[2] & 0xFF) << 8)
                    | ((bytes[1] & 0xFF) << 16)
                    | ((bytes[0] & 0xFF) << 24);
        }
    }

    @NonNull
    public static byte[] hex2bytes(@Nullable String str) {
        int length;

        if (str == null || str.length() == 0) {
            return new byte[0];
        }

        str = str.replace("0x","");
        length = str.length();
        if ((length & 0x01) == 1) {
            throw new ByteFormatException("hex2bytes: the length of string must be even number");
        } else if (!HEX_PATTERN.matcher(str).matches()) {
            throw new ByteFormatException(String.format("hex2bytes: [%s] has illegal character", str));
        }

        final byte[] bytes = new byte[length >> 1];

        for (int i = 0, len = bytes.length; i < len; i++) {
            final int si = i << 1;
            final byte b1 = HEX_DECODE_TABLE[str.charAt(si)];
            final byte b2 = HEX_DECODE_TABLE[str.charAt(si + 1)];

            bytes[i] = (byte) ((b1 << 4) | b2);
        }

        return bytes;
    }

    @NonNull
    public static String bytes2hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        final char[] hexChars = new char[bytes.length << 1];

        for (int i = 0, len = bytes.length; i < len; ++i) {
            final int b = bytes[i] & 0xFF;

            hexChars[i << 1] = HEX_ENCODE_TABLE[b >>> 4];
            hexChars[(i << 1) + 1] = HEX_ENCODE_TABLE[b & 0xF];
        }

        return new String(hexChars);
    }

    @NonNull
    public static byte[] utf82bytes(@Nullable String str) {
        if (str == null) {
            return new byte[0];
        }

        return str.getBytes(StandardCharsets.UTF_8);
    }

    @NonNull
    public static String bytes2utf8(@Nullable byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @NonNull
    public static String addHexPrefix(int i) {
        return addHexPrefix(Integer.toHexString(i));
    }

    @NonNull
    public static String addHexPrefix(String hex) {
        final int length;
        if (hex == null || (length = hex.length()) == 0) {
            return "0x00";
        } else if ((length & 0x01) == 1) {
            hex = '0' + hex;
        }
        return String.format("0x%s", hex.toLowerCase(Locale.getDefault()));
    }
}
