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

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PacketTest {

    @Test
    public void getId() {
        assertEquals(0x01, new Packet.Builder(0x01).build().getId());
    }

    @Test
    public void getPayload() {
        assertNull(new Packet.Builder(0x00)
                .addBytePayload(0x01, 0x02)
                .build()
                .getPayload(0x00)
        );
    }

    @Test
    public void getPayload2() {
        assertEquals(0x02,
                new Packet.Builder(0x00)
                        .addBytePayload(0x01, 0x02)
                        .build()
                        .getPayload(0x01)
                        .toInt());
    }

    @Test
    public void getPayload3() {
        assertEquals(0xEFFE, new Packet.Builder(0x00)
                .addShortPayload(0x01, 0xEFFE)
                .build()
                .getPayload(0x01)
                .toInt()
        );
    }

    @Test
    public void getPayload4() {
        assertEquals(0x01020304, new Packet.Builder(0x00)
                .addIntPayload(0x01, 0x01020304)
                .build()
                .getPayload(0x01)
                .toInt()
        );
    }

    @Test
    public void getPayload5() {
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, new Packet.Builder(0x00)
                .addBytesPayload(0x01, new byte[]{0x01, 0x02, 0x03, 0x04})
                .build()
                .getPayload(0x01)
                .getValue()
        );
    }

    @Test
    public void getPayload6() {
        assertEquals("04030201", new Packet.Builder(0x00)
                .addHexPayload(0x01, "04030201")
                .build()
                .getPayload(0x01)
                .toHex()
        );
    }

    @Test
    public void getPayload7() {
        assertEquals("hello world", new Packet.Builder(0x00)
                .addTextPayload(0x01, "hello world")
                .build()
                .getPayload(0x01)
                .toUtf8()
        );
    }

    @Test
    public void getPayloads() {
        assertEquals(new Payload(new byte[]{0x01, (byte) 0xFE}), new Packet.Builder(0x00)
                .addShortPayload(0x01, 0x01FE)
                .build()
                .getPayloads()
                .valueAt(0));
    }
}