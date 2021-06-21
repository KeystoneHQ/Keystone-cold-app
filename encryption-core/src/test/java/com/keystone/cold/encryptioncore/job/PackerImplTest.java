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

package com.keystone.cold.encryptioncore.job;

import androidx.collection.SparseArrayCompat;

import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;
import com.keystone.cold.encryptioncore.interfaces.Cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class PackerImplTest {
    @Mock
    private Cipher mCipher;

    @Test
    public void serialize() throws Exception {
        // 02 00 00 0B 00 01 00 02 01 03 00 04 00 01 00 03 0E
        assertArrayEquals(new byte[]{
                0x02, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03, 0x03, 0x06
        }, new PackerImpl(null).serialize(new Packet.Builder(0x0103).build()));

        assertArrayEquals(new byte[]{
                0x02, 0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x02, 0x02, 0x08, 0x02, 0x0F, 0x00, 0x01, 0x01, 0x03, 0x0e
        }, new PackerImpl(null).serialize(new Packet.Builder(0x0208).addBytePayload(0x020F, 0x01).build()));
    }

    @Test
    public void serializeBody() throws IOException {
        assertArrayEquals(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x01, 0x03}, new PackerImpl(null).serializeBody(new Packet.Builder(0x0103).build()));

        // 02 00 00 10 00 01 00 02 02 08 00 04 00 01 00 02 0F 00 01 01 03 10
        assertArrayEquals(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x02, 0x08, 0x02, 0x0F, 0x00, 0x01, 0x01
        }, new PackerImpl(mCipher).serializeBody(new Packet.Builder(0x0208).addBytePayload(0x020F, 0x01).build()));
    }

    @Test
    public void serializePayloads() throws IOException {
        final SparseArrayCompat<Payload> payloads = new SparseArrayCompat<>();
        payloads.put(0x01, new Payload(new byte[]{0x02, 0x03}));

        assertArrayEquals(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x02, 0x03
        }, new PackerImpl(null).serializePayloads(payloads));
    }

    @Test
    public void serializePayloads2() throws IOException {
        final SparseArrayCompat<Payload> payloads = new SparseArrayCompat<>();
        payloads.put(0x01, new Payload(new byte[]{0x02, 0x03}));
        payloads.put(0x02, null);

        assertArrayEquals(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x02, 0x03
        }, new PackerImpl(null).serializePayloads(payloads));
    }

    @Test
    public void serializeTLV() {
        assertArrayEquals(new byte[]{
                0x00, 0x00, 0x00, 0x01, 0x00
        }, new PackerImpl(null).serializeTLV(0x00, new byte[]{0x00}));
    }

    @Test
    public void serializeTLV2() {
        assertArrayEquals(new byte[]{
                0x00, 0x00, 0x00, 0x00
        }, new PackerImpl(null).serializeTLV(0x00, new byte[0]));
    }

    @Test
    public void calculateLRC() {
        assertEquals(63, new PackerImpl(null).calculateLRC(new byte[]{
                (byte) 0xFF, (byte) 0xEE, (byte) 0xCC, 0x00, (byte) 0xFF,
                0x11, 0x0C
        }));

        assertEquals(0x18, new PackerImpl(null).calculateLRC(new byte[]{
                0x02, 0x00, 0x00, 0x12, 0x00, 0x01, 0x00, 0x02, 0x08, 0x01, 0x08,
                0x01, 0x00, 0x08, 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB,
                (byte) 0xCD, (byte) 0xEF, 0x03
        }));

        assertEquals(0x00, new PackerImpl(null).calculateLRC(new byte[0]));
    }

    @Test
    public void deserialize() throws Exception {
        final Packet packet = new PackerImpl(null).deserialize(new byte[]{
                0x02, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x03, 0x09
        });

        assertEquals(0x0601, packet.getId());
        assertEquals(2, packet.getPayloads().size());
        assertNotNull(packet.getPayload(0x0001));
        assertNotNull(packet.getPayload(0x0002));
        assertEquals(0x0601, packet.getPayload(0x0001).toInt());
        assertEquals(0x0000, packet.getPayload(0x0002).toInt());
        assertNull(packet.getPayload(0x0004));
    }

    @Test
    public void deserializeBody() {
        final Packet packet = new PackerImpl(null).deserializeBody(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x01, 0x3, 0x00, 0x02, 0x00, 0x02, 0x00, 0x00
        }).build();

        assertEquals(0x0103, packet.getId());
        assertEquals(2, packet.getPayloads().size());
        assertNotNull(packet.getPayload(0x01));
        assertNotNull(packet.getPayload(0x02));
        assertEquals(0x0103, packet.getPayload(0x0001).toInt());
        assertEquals(0x0000, packet.getPayload(0x0002).toInt());
        assertNull(packet.getPayload(0x03));
    }

    @Test(expected = IllegalStateException.class)
    public void deserializeBody2() {
        new PackerImpl(null).deserializeBody(new byte[]{
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00
        });
    }


    @Test
    public void deserializePayloads() {
        final SparseArrayCompat<Payload> payloads = new PackerImpl(null).deserializePayloads(new byte[]{
                0x00, 0x01, 0x00, 0x01, 0x0F
        });

        assertNotNull(payloads);
        assertEquals(1, payloads.size());
        assertNotNull(payloads.get(0x01));
        assertEquals(payloads.get(0x01), new Payload(new byte[]{0x0F}));
    }

    @Test(expected = IllegalStateException.class)
    public void deserializePayloads2() {
        new PackerImpl(null).deserializePayloads(new byte[]{
                0x00, 0x01, 0x00, 0x01, 0x0F, 0x00
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSerializedData() {
        new PackerImpl(null).checkSerializedData(new byte[0]); // illegal size
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSerializedData2() {
        new PackerImpl(null).checkSerializedData(new byte[]{0x01/*illegal STX*/, 0x00, 0x00, 0x00, 0x03, 0x01});
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSerializedData3() {
        new PackerImpl(null).checkSerializedData(new byte[]{0x02, 0x02/*illegal encryption status*/, 0x00, 0x00, 0x03, 0x01});
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSerializedData4() {
        new PackerImpl(null).checkSerializedData(new byte[]{0x02, 0x00, 0x00, 0x00, 0x04/*illegal ETX*/, 0x01});
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSerializedData5() {
        new PackerImpl(null).checkSerializedData(new byte[]{0x02, 0x00, 0x00, 0x00, 0x03, (byte) 0xFF/*illegal LRC*/});
    }

    @Test
    public void checkSerializedData6() {
        new PackerImpl(null).checkSerializedData(new byte[]{0x02, 0x00, 0x00, 0x00, 0x03, 0x01});
    }
}
