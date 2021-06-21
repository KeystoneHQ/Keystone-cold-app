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

import com.keystone.cold.encryptioncore.exception.ByteFormatException;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;


public class PayloadTest {

    @Test
    public void getValue() {
        final byte[] value = new byte[]{0x01, 0x01};
        assertNotSame(value, new Payload(value).getValue());
        assertArrayEquals(value, new Payload(value).getValue());
    }

    @Test
    public void toInt() {
        assertEquals(0x01010101, new Payload(new byte[]{0x01, 0x01, 0x01, 0x01}).toInt());
    }

    @Test(expected = ByteFormatException.class)
    public void toInt2() {
        new Payload(new byte[0]).toInt();
    }

    @Test(expected = ByteFormatException.class)
    public void toInt3() {
        new Payload(new byte[]{0x01, 0x01, 0x01, 0x01, 0x01}).toInt();
    }

    @Test
    public void toInt4() {
        assertEquals(0x01, new Payload(new byte[]{0x01}).toInt());
    }

    @Test
    public void toInt5() {
        assertEquals(0x0102, new Payload(new byte[]{0x01, 0x02}).toInt());
    }

    @Test(expected = ByteFormatException.class)
    public void toInt6() {
        new Payload(new byte[]{0x01, 0x02, 0x03}).toInt();
    }

    @Test
    public void toHex() {
        assertEquals("0101", new Payload(new byte[]{0x01, 0x01}).toHex());
        assertEquals("", new Payload(new byte[0]).toHex());
    }

    @Test
    public void toUtf8() {
        assertEquals("abandon", new Payload(new byte[]{0x61, 0x62, 0x61, 0x6E, 0x64, 0x6F, 0x6E}).toUtf8());
        assertEquals("m/0'", new Payload(new byte[]{0x6D, 0x2F, 0x30, 0x27}).toUtf8());
        assertEquals("", new Payload(new byte[0]).toUtf8());
    }

    @Test
    public void equals() {
        assertEquals(new Payload(new byte[]{0x01, 0x02}), new Payload(new byte[]{0x01, 0x02}));
        assertNotEquals(new Payload(new byte[]{0x01}), new Payload(new byte[]{0x02}));
    }
}