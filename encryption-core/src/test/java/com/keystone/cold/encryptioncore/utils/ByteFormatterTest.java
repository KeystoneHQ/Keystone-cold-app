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

import com.keystone.cold.encryptioncore.exception.ByteFormatException;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ByteFormatterTest {
    @Test
    public void short2bytes() {
        assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x00}, ByteFormatter.short2bytes(0));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, ByteFormatter.short2bytes(0xFFFF));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, ByteFormatter.short2bytes(0xFFFFFF));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00}, ByteFormatter.short2bytes(0xFF00));
        assertArrayEquals(new byte[]{(byte) 0x11, (byte) 0x11}, ByteFormatter.short2bytes(0x1111));
    }

    @Test
    public void bytes2short() {
        assertEquals(0, ByteFormatter.bytes2short(null));
        assertEquals(0x00000, ByteFormatter.bytes2short(new byte[]{0x00, (byte) 0x00}));
        assertEquals(0xFFFF, ByteFormatter.bytes2short(new byte[]{(byte) 0xFF, (byte) 0xFF}));
        assertEquals(0x00FF, ByteFormatter.bytes2short(new byte[]{(byte) 0x00, (byte) 0xFF}));
        assertEquals(0x1100, ByteFormatter.bytes2short(new byte[]{(byte) 0x11, (byte) 0x00}));
    }

    @Test(expected = ByteFormatException.class)
    public void bytes2short2() {
        ByteFormatter.bytes2short(new byte[]{(byte) 0xFF});
    }

    @Test(expected = ByteFormatException.class)
    public void bytes2short3() {
        ByteFormatter.bytes2short(new byte[]{(byte) 0xFFFFFF});
    }

    @Test
    public void int2bytes() {
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00}, ByteFormatter.int2bytes(0));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, ByteFormatter.int2bytes(0xFFFFFFFF));
        assertArrayEquals(new byte[]{(byte) 0xFF, 0x11, 0x11, 0x00}, ByteFormatter.int2bytes(0xFF111100));
        assertArrayEquals(new byte[]{0x00, (byte) 0xFF, (byte) 0xFF, 0x00}, ByteFormatter.int2bytes(0x00FFFF00));
        assertArrayEquals(new byte[]{0x00, (byte) 0x00, (byte) 0x00, 0x0F}, ByteFormatter.int2bytes(0xF));
    }

    @Test
    public void bytes2int() {
        assertEquals(0x0, ByteFormatter.bytes2int(new byte[]{0x00, 0x00, 0x00, 0x00}));
        assertEquals(0xFFFFFFFF, ByteFormatter.bytes2int(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
        assertEquals(0x00FFFF00, ByteFormatter.bytes2int(new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00}));
        assertEquals(0xFF0011FF, ByteFormatter.bytes2int(new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x11, (byte) 0xFF}));
        assertEquals(0, ByteFormatter.bytes2int(null));
    }

    @Test(expected = ByteFormatException.class)
    public void bytes2int2() {
        ByteFormatter.bytes2int(new byte[0]);
    }

    @Test(expected = ByteFormatException.class)
    public void bytes2int3() {
        ByteFormatter.bytes2int(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
    }

    @Test
    public void hex2bytes() {
        assertArrayEquals(new byte[0], ByteFormatter.hex2bytes(null));
        assertArrayEquals(new byte[]{0x00}, ByteFormatter.hex2bytes("00"));
        assertArrayEquals(new byte[]{0x0a, (byte) 0xbc}, ByteFormatter.hex2bytes("0abc"));
        assertArrayEquals(new byte[]{(byte) 0xFF, 0x00}, ByteFormatter.hex2bytes("FF00"));
        assertArrayEquals(new byte[]{
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        }, ByteFormatter.hex2bytes("00000000000000000000000000"));
        assertArrayEquals(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x12, (byte) 0x31, (byte) 0x2F, (byte) 0xF1
        }, ByteFormatter.hex2bytes("FFFFFF12312FF1"));
    }

    @Test(expected = ByteFormatException.class)
    public void hex2bytes2() {
        ByteFormatter.hex2bytes("1");
    }

    @Test(expected = ByteFormatException.class)
    public void hex2bytes3() {
        ByteFormatter.hex2bytes("if");
    }

    @Test
    public void bytes2hex() {
        assertEquals("", ByteFormatter.bytes2hex(null));
        assertEquals("", ByteFormatter.bytes2hex(new byte[0]));
        assertEquals("00", ByteFormatter.bytes2hex(new byte[]{0x00}));
        assertEquals("ff00", ByteFormatter.bytes2hex(new byte[]{(byte) 0xFF, 0x00}));
        assertEquals("f0", ByteFormatter.bytes2hex(new byte[]{(byte) 0xF0}));
        assertEquals("00000000000000000000000000", ByteFormatter.bytes2hex(new byte[]{
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        }));
        assertEquals("ffffff12312ff1", ByteFormatter.bytes2hex(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x12, (byte) 0x31, (byte) 0x2F, (byte) 0xF1
        }));
    }

    @Test
    public void utf82bytes() {
        assertArrayEquals(new byte[]{'a', 'b', 'c'}, ByteFormatter.utf82bytes("abc"));
        assertArrayEquals(new byte[]{'a', 'b', 'a', 'n', 'd', 'o', 'n'},
                ByteFormatter.utf82bytes("abandon"));
        assertArrayEquals(new byte[]{'m', '/', '0', '\''}, ByteFormatter.utf82bytes("m/0'"));
        assertArrayEquals(new byte[0], ByteFormatter.utf82bytes(null));
        assertArrayEquals(new byte[0], ByteFormatter.utf82bytes(""));
    }

    @Test
    public void bytes2utf8() {
        assertEquals("m/0'", ByteFormatter.bytes2utf8(new byte[]{'m', '/', '0', '\''}));
        assertEquals("abandon", ByteFormatter.bytes2utf8(new byte[]{'a', 'b', 'a', 'n', 'd', 'o', 'n'}));
        assertEquals("", ByteFormatter.bytes2utf8(null));
        assertEquals("", ByteFormatter.bytes2utf8(new byte[0]));
    }

    @Test
    public void addHexPrefix() {
        assertEquals("0x00", ByteFormatter.addHexPrefix(0));
        assertEquals("0xffffffff", ByteFormatter.addHexPrefix(0xFFFFFFFF));
        assertEquals("0x0f", ByteFormatter.addHexPrefix(0xF));
    }

    @Test
    public void addHexPrefix2() {
        assertEquals("0x00", ByteFormatter.addHexPrefix(null));
        assertEquals("0x0f", ByteFormatter.addHexPrefix("F"));
        assertEquals("0xff", ByteFormatter.addHexPrefix("ff"));
    }
}