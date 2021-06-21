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

import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkshopTest {

    @Mock
    private SerialPortProxy mPort;

    @Test
    public void call() throws Exception {
        final byte[] outputBytes = new byte[]{
                0x02, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03,
                 0x03, 0x06
        };

        final byte[] inputBytes = new byte[]{
                0x02, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03,
                0x01, 0x02, 0x00, 0x01, (byte) 0xcb, 0x00, 0x02, 0x00, 0x02,
                0x00, 0x00, 0x03, (byte) 0xd8
        };

        when(mPort.read(any(ByteBuffer.class), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
            final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);
            buffer.put(inputBytes);

            return inputBytes.length;
        });

        final Packet packet = new Packet.Builder(0x0103).build();

        final Callable<Packet> callable = new Workshop(mPort, new PackerImpl(null), packet);
        final Packet result = callable.call();

        verify(mPort, times(1)).read(any(ByteBuffer.class), anyInt());

        final ArgumentCaptor<ByteBuffer> argument = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(mPort, times(1)).write(argument.capture(), eq(outputBytes.length));
        final ByteBuffer outputBuffer = argument.getValue();
        assertArrayEquals(outputBytes, outputBuffer.array());

        assertNotNull(result);
        assertEquals(0x0103, result.getId());
        assertEquals(3, result.getPayloads().size());
        assertNotNull(result.getPayload(0x0001));
        assertNotNull(result.getPayload(0x0102));
        assertNotNull(result.getPayload(0x0002));
        assertNull(result.getPayload(0x0003));
        assertEquals(0xcb, result.getPayload(0x0102).toInt());
    }

}
