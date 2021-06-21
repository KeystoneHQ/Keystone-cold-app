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

import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class SerialReaderTest {

    @Mock
    private SerialPortProxy mPort;

    @Test
    public void checkMilestone() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                0x02, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x03, 0x09
        });

        assertEquals(1, SerialReader.checkMilestone(buffer, 0, 0));
        assertEquals(2, SerialReader.checkMilestone(buffer, 1, 1));
        assertEquals(0x000c, SerialReader.checkMilestone(buffer, 2, 2));
        assertEquals(1, SerialReader.checkMilestone(buffer, 3, 4));
        assertEquals(1, SerialReader.checkMilestone(buffer, 4, 0x000c + 4));
        assertEquals(0, SerialReader.checkMilestone(buffer, 5, 0x000c + 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone2() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                0x00,
        });

        SerialReader.checkMilestone(buffer, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone3() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                0x02, 0x03
        });

        SerialReader.checkMilestone(buffer, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone4() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                0x02, 0x00, 0x00, 0x00
        });

        SerialReader.checkMilestone(buffer, 2, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone5() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                0x02, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x01, 0x09
        });

        SerialReader.checkMilestone(buffer, 4, 0x000c + 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone6() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);

        SerialReader.checkMilestone(buffer, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMilestone7() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);

        SerialReader.checkMilestone(buffer, 6, 0);
    }

    @Test
    public void call() throws Exception {
        final byte[] bytes = new byte[]{
                0x02, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x03, 0x09
        };

        when(mPort.read(any(ByteBuffer.class), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
            final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);
            buffer.put(bytes);

            return bytes.length;
        });

        final Callable<byte[]> callable = new SerialReader(mPort);
        final byte[] result = callable.call();

        assertArrayEquals(bytes, result);
        verify(mPort, times(1)).read(any(ByteBuffer.class), anyInt());
    }

    @Test
    public void call2() throws Exception {
        final AtomicInteger milestone = new AtomicInteger(0);

        when(mPort.read(any(ByteBuffer.class), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
            final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);

            switch (milestone.getAndIncrement()) {
                case 0:
                    buffer.put((byte) 0x02);
                    return 1;

                case 1:
                    buffer.put((byte) 0x00);
                    return 1;

                case 2:
                    buffer.put(new byte[]{0x00, 0x0c});
                    return 2;

                case 3:
                    buffer.put(new byte[]{
                            0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                            0x00, 0x02, 0x00, 0x02, 0x00, 0x00
                    });
                    return 0x000c;

                case 4:
                    buffer.put((byte) 0x03);
                    return 1;

                case 5:
                    buffer.put((byte) 0x09);
                    return 1;

                default:
                    return 0;
            }
        });

        final Callable<byte[]> callable = new SerialReader(mPort);
        final byte[] result = callable.call();

        assertArrayEquals(new byte[]{
                0x02, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x02, 0x06, 0x01,
                0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x03, 0x09
        }, result);

        final ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        verify(mPort, times(6)).read(any(ByteBuffer.class), argument.capture());

        final List<Integer> captureList = argument.getAllValues();
        final Integer[] captureArray = new Integer[captureList.size()];
        captureList.toArray(captureArray);
        assertArrayEquals(new Integer[]{
                0, 1, 2, 4, 16, 17
        }, captureArray);
    }
}