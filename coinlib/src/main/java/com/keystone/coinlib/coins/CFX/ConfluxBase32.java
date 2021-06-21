/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.CFX;

import com.google.common.io.BaseEncoding;

import java.util.HashMap;

public class ConfluxBase32 {
    private static final String CONFLUX_CHARSET = "abcdefghjkmnprstuvwxyz0123456789";
    private static final String STANDARD_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final char PADDING_CHAR = '=';

    private static final HashMap<Character, Integer> CONFLUX_CHAR_MAP = new HashMap<>();
    static {
        for (int i = 0; i < CONFLUX_CHARSET.length(); i++) {
            CONFLUX_CHAR_MAP.put(CONFLUX_CHARSET.charAt(i), i);
        }
    }

    private static final HashMap<Character, Integer> STANDARD_CHAR_MAP = new HashMap<>();
    static {
        for (int i = 0; i < STANDARD_CHARSET.length(); i++) {
            STANDARD_CHAR_MAP.put(STANDARD_CHARSET.charAt(i), i);
        }
    }

    public static String encode(byte[] buffer) throws ConfluxBase32Exception {
        if (buffer == null) {
            throw new ConfluxBase32Exception("buffer is null or empty");
        }
        return fromStandard(BaseEncoding.base32().encode(buffer));
    }

    public static byte[] decode(String base32Str) throws ConfluxBase32Exception {
        if (!isValid(base32Str)) {
            throw new ConfluxBase32Exception(ConfluxBase32Exception.INVALID_BASE32_STR);
        }
        return BaseEncoding.base32().decode(toStandard(base32Str));
    }

    public static byte[] decodeWords(String base32Words) throws ConfluxBase32Exception {
        if (!isValid(base32Words)) {
            throw new ConfluxBase32Exception(ConfluxBase32Exception.INVALID_BASE32_STR);
        }
        byte[] result = new byte[base32Words.length()];
        for(int i = 0; i < base32Words.length(); i++) {
            int num = CONFLUX_CHAR_MAP.get(base32Words.charAt(i));
            result[i] = (byte)num;
        }
        return result;
    }

    public static String encodeWords(byte[] words) throws ConfluxBase32Exception {
        if (words == null) {
            throw new ConfluxBase32Exception("buffer is null or empty");
        }
        StringBuilder result = new StringBuilder(words.length);
        for (byte word : words) {
            if (word < 0 || word > 31) {
                throw new ConfluxBase32Exception(ConfluxBase32Exception.INVALID_BASE32_WORDS);
            }
            result.append(CONFLUX_CHARSET.charAt(word));
        }
        return result.toString();
    }

    public static boolean isValid(String base32Str) {
        if (base32Str == null) return false;
        for (int i = 0; i < base32Str.length(); i++) {
            if(!CONFLUX_CHAR_MAP.containsKey(base32Str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String toStandard(String base32Str) {
        StringBuilder result = new StringBuilder(base32Str.length());
        for(int i = 0; i < base32Str.length(); i++) {
            char c = base32Str.charAt(i);
            int index = CONFLUX_CHAR_MAP.get(c);
            result.append(STANDARD_CHARSET.charAt(index));
        }
        return result.toString();
    }

    private static String fromStandard(String standardBase32Str) {
        StringBuilder result = new StringBuilder(standardBase32Str.length());
        for(int i = 0; i < standardBase32Str.length(); i++) {
            char c = standardBase32Str.charAt(i);
            if (c == PADDING_CHAR) {
                break;
            }
            int index = STANDARD_CHAR_MAP.get(c);
            result.append(CONFLUX_CHARSET.charAt(index));
        }
        return result.toString();
    }
}
