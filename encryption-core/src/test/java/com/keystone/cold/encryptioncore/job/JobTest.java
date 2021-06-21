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
import com.keystone.cold.encryptioncore.interfaces.Callback;
import com.keystone.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobTest {

    @Mock
    private SerialManagerProxy mManager;

    @Mock
    private SerialPortProxy mPort;

    @Mock
    private Callback mCallback;

    @Test
    public void openPort() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[]{"1100", "2200"});
        when(mManager.openSerialPort(anyString())).thenReturn(mPort);

        assertEquals(mPort, Job.openPort(mManager));
        final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(mManager, times(1)).openSerialPort(argument.capture());
        assertEquals("1100", argument.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void openPort2() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(null);

        Job.openPort(mManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openPort3() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[0]);

        Job.openPort(mManager);
    }

    @Test(expected = NullPointerException.class)
    public void openPort4() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[]{"0001"});
        when(mManager.openSerialPort(anyString())).thenReturn(null);

        Job.openPort(mManager);
    }

    @Test
    public void run() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[]{"1100"});
        when(mManager.openSerialPort(anyString())).thenReturn(mPort);

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

        new Job(mManager, new PackerImpl(null), packet, mCallback).run();

        verify(mManager, times(1)).getSerialPorts();
        verify(mManager, times(1)).openSerialPort(anyString());
        verify(mManager,times(1)).acquireWakeLock();
        verify(mManager,times(1)).releaseWakeLock();

        verify(mPort, times(1)).read(any(ByteBuffer.class), anyInt());
        verify(mPort, times(1)).write(any(ByteBuffer.class), anyInt());
        verify(mPort, times(1)).close();

        verify(mCallback, never()).onFail(any(Exception.class));

        final ArgumentCaptor<Packet> argumentCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(mCallback, times(1)).onSuccess(argumentCaptor.capture());

        verifyNoMoreInteractions(mManager, mPort, mCallback);

        final Packet result = argumentCaptor.getValue();

        assertNotNull(result);
        assertEquals(0x0103, result.getId());
        assertEquals(3, result.getPayloads().size());
    }

    @Test
    public void run2() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[]{"1100"});
        when(mManager.openSerialPort(anyString())).thenReturn(mPort);

        final byte[] inputBytes = new byte[]{
                0x02, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03,
                0x01, 0x02, 0x00, 0x01, (byte) 0xcb, 0x00, 0x02, 0x00, 0x02,
                0x00, 0x00, 0x03, (byte) 0xd9 /*illegal lrc*/
        };

        when(mPort.read(any(ByteBuffer.class), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
            final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);
            buffer.put(inputBytes);

            return inputBytes.length;
        });

        final Packet packet = new Packet.Builder(0x0103).setRetryTimes(2).build();

        new Job(mManager, new PackerImpl(null), packet, mCallback).run();

        verify(mManager, times(3)).getSerialPorts();
        verify(mManager, times(3)).openSerialPort(anyString());
        verify(mManager,times(1)).acquireWakeLock();
        verify(mManager,times(1)).releaseWakeLock();

        verify(mPort, times(3)).read(any(ByteBuffer.class), anyInt());
        verify(mPort, times(3)).write(any(ByteBuffer.class), anyInt());
        verify(mPort, times(3)).close();

        verify(mCallback, never()).onSuccess(any(Packet.class));

        final ArgumentCaptor<Exception> argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(mCallback, times(1)).onFail(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue() instanceof IllegalArgumentException);

        verifyNoMoreInteractions(mManager, mPort, mCallback);
    }

    @Test
    public void run3() throws IOException {
        when(mManager.getSerialPorts()).thenReturn(new String[]{"1100"});
        when(mManager.openSerialPort(anyString())).thenReturn(mPort);

        final byte[] errorBytes = new byte[]{
                0x02, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03,
                0x01, 0x02, 0x00, 0x01, (byte) 0xcb, 0x00, 0x02, 0x00, 0x02,
                0x00, 0x00, 0x03, (byte) 0xd9 /*illegal lrc*/
        };

        final byte[] correctBytes = new byte[]{
                0x02, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00, 0x02, 0x01, 0x03,
                0x01, 0x02, 0x00, 0x01, (byte) 0xcb, 0x00, 0x02, 0x00, 0x02,
                0x00, 0x00, 0x03, (byte) 0xd8
        };

        final AtomicInteger times = new AtomicInteger(1);

        when(mPort.read(any(ByteBuffer.class), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
            final int i = times.get();
            if (i < 3) {
                times.incrementAndGet();
                final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);
                buffer.put(errorBytes);
                return errorBytes.length;
            } else if (i == 3) {
                final ByteBuffer buffer = invocation.getArgumentAt(0, ByteBuffer.class);
                buffer.put(correctBytes);
                return correctBytes.length;
            }
            throw new IllegalStateException();
        });

        final Packet packet = new Packet.Builder(0x0103).setRetryTimes(3).build();
        new Job(mManager, new PackerImpl(null), packet, mCallback).run();

        verify(mManager, times(3)).getSerialPorts();
        verify(mManager, times(3)).openSerialPort(anyString());
        verify(mManager,times(1)).acquireWakeLock();
        verify(mManager,times(1)).releaseWakeLock();

        verify(mPort, times(3)).read(any(ByteBuffer.class), anyInt());
        verify(mPort, times(3)).write(any(ByteBuffer.class), anyInt());
        verify(mPort, times(3)).close();

        verify(mCallback, never()).onFail(any(Exception.class));

        final ArgumentCaptor<Packet> argumentCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(mCallback, times(1)).onSuccess(argumentCaptor.capture());

        verifyNoMoreInteractions(mManager, mPort, mCallback);
    }
}