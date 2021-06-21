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

package com.keystone.cold.encryptioncore.serial;

import android.hardware.SerialPort;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.io.IOException;
import java.nio.ByteBuffer;

class SerialPortProxyImpl implements SerialPortProxy {
    private final SerialPort mProxy;

    SerialPortProxyImpl(@NonNull SerialPort proxy) {
        mProxy = Preconditions.checkNotNull(proxy);
    }

    @Override
    public void open(ParcelFileDescriptor pfd, int speed) throws IOException {
        mProxy.open(pfd, speed);
    }

    @Override
    public void close() throws IOException {
        mProxy.close();
    }

    @Override
    public int read(ByteBuffer buffer, int offset) throws IOException {
        return mProxy.read(buffer, offset);
    }

    @Override
    public void write(ByteBuffer buffer, int length) throws IOException {
        mProxy.write(buffer, length);
    }

    @Override
    public void sendBreak() {
        mProxy.sendBreak();
    }
}
