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

import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.BuildConfig;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.interfaces.Packer;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class Workshop implements Callable<Packet> {
    private static final String TAG = "EncryptionCore.Workshop";
    private static final int DEFAULT_TIMEOUT = 60;
    private final SerialPortProxy mPort;
    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private final Packet mPacket;
    private final Packer mPacker;

    Workshop(@NonNull SerialPortProxy port, @NonNull Packer packer, @NonNull Packet packet) {
        mPort = Preconditions.checkNotNull(port);
        mPacker = packer;
        mPacket = Preconditions.checkNotNull(packet);
    }

    private void logBytes(boolean isRequest, @NonNull String id, @NonNull byte[] bytes) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        try {
            Log.w(TAG, "id: " + id + " " + (isRequest ? "downstream: " : "upstream: ") +
                    ByteFormatter.bytes2hex(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logPacket(boolean isRequest, @NonNull Packet packet) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        Log.w(TAG, (isRequest ? "downstream: " : "upstream: ") + packet.toString());
    }

    @Override
    public Packet call() throws Exception {
        final String id = ByteFormatter.addHexPrefix(mPacket.getId());
        final byte[] outputBytes = mPacker.serialize(mPacket);
        logBytes(true, id, outputBytes);
        logPacket(true, mPacket);
        mPort.write(ByteBuffer.wrap(outputBytes), outputBytes.length);

        final Future<byte[]> future = sExecutor.submit(new SerialReader(mPort));
        int timeout = mPacket.getTimeout() == 0 ? DEFAULT_TIMEOUT : mPacket.getTimeout();
        final byte[] inputBytes = future.get(timeout, TimeUnit.SECONDS);
        logBytes(false, id, inputBytes);

        Packet packet = mPacker.deserialize(inputBytes);
        logPacket(false, packet);
        return packet;
    }
}