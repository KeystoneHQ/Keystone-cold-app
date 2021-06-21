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

package com.keystone.cold.callables;

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.EncryptionCoreProvider;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.interfaces.Callback;
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlockingCallable implements Callable<Packet> {
    private final Packet mPacket;

    public BlockingCallable(@NonNull Packet packet) {
        mPacket = packet;
    }

    @NonNull
    @Override
    public Packet call() throws Exception {
        final JobScheduler jobScheduler = EncryptionCoreProvider.getInstance().getImpl();

        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<Object> reference = new AtomicReference<>();

        jobScheduler.offer(mPacket, new Callback() {
            @Override
            public void onSuccess(@NonNull Packet packet) {
                reference.set(packet);
                mLatch.countDown();
            }

            @Override
            public void onFail(@NonNull Exception exception) {
                reference.set(exception);
                mLatch.countDown();
            }
        });

        mLatch.await();

        final Object result = reference.get();

        if (result instanceof Packet) {
            return (Packet) result;
        } else if (result instanceof Exception) {
            throw (Exception) result;
        } else {
            throw new IllegalStateException("zero result");
        }
    }
}