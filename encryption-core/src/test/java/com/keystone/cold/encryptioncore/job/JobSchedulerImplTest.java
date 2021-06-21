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
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;
import com.keystone.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerImplTest {
    @Mock
    private SerialManagerProxy mManager;

    @Mock
    private SerialPortProxy mPort;

    @Test
    public void offer() throws IOException, InterruptedException {
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

        final Packet packet1 = new Packet.Builder(0x0103).build();
        final Callback callback1 = mock(Callback.class);

        final Packet packet2 = new Packet.Builder(0x0103).build();
        final Callback callback2 = mock(Callback.class);

        final Packet packet3 = new Packet.Builder(0x0103).build();
        final Callback callback3 = mock(Callback.class);

        final Packet packet4 = new Packet.Builder(0x0103).build();
        final Callback callback4 = mock(Callback.class);

        final Packet packet5 = new Packet.Builder(0x0103).build();
        final Callback callback5 = mock(Callback.class);

        final CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(callback5).onSuccess(any(Packet.class));

        final JobScheduler jobScheduler = new JobSchedulerImpl(mManager, null);

        jobScheduler.offer(packet1, callback1);
        jobScheduler.offer(packet2, callback2);
        jobScheduler.offer(packet3, callback3);
        jobScheduler.offer(packet4, callback4);
        jobScheduler.offer(packet5, callback5);

        latch.await(10, TimeUnit.SECONDS);

        final InOrder inOrder = Mockito.inOrder(callback1, callback2, callback3, callback4, callback5);

        inOrder.verify(callback1).onSuccess(any(Packet.class));
        inOrder.verify(callback2).onSuccess(any(Packet.class));
        inOrder.verify(callback3).onSuccess(any(Packet.class));
        inOrder.verify(callback4).onSuccess(any(Packet.class));
        inOrder.verify(callback5).onSuccess(any(Packet.class));
    }
}