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

package com.keystone.cold.encryptioncore.base;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.exception.ByteFormatException;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.util.Arrays;

public class Payload {
    private final byte[] value;

    public Payload(@NonNull byte[] value) {
        this.value = Preconditions.checkNotNull(value);
    }

    @NonNull
    public byte[] getValue() {
        return value.clone();
    }

    public int toInt() {
        if (value.length == 1) {
            return value[0] & 0xFF;
        } else if (value.length == 2) {
            return ByteFormatter.bytes2short(value);
        } else if (value.length == 4) {
            return ByteFormatter.bytes2int(value);
        } else {
            throw new ByteFormatException("the length of values must be [one, two, four]");
        }
    }

    public String toHex() {
        return ByteFormatter.bytes2hex(value);
    }

    public String toUtf8() {
        return ByteFormatter.bytes2utf8(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Payload && Arrays.equals(this.value, ((Payload) obj).value);
    }
}
