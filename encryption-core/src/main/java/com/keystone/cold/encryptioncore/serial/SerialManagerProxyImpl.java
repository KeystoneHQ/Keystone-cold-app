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

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SerialManager;
import android.hardware.SerialPort;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.io.IOException;
import java.util.Objects;

public class SerialManagerProxyImpl implements SerialManagerProxy {
    private static PowerManager.WakeLock wakeLock;
    private final SerialManager mProxy;
    private final int mPortSpeed;

    private SerialManagerProxyImpl(@NonNull SerialManager proxy, Context context, int portSpeed) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = Objects.requireNonNull(pm).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "keystone:encryption");
        mProxy = proxy;
        mPortSpeed = portSpeed;
    }

    public static SerialManagerProxy newInstance(@NonNull Context context, int portSpeed) {
        Preconditions.checkNotNull(context);

        @SuppressLint("WrongConstant") final SerialManager manager = (SerialManager) Preconditions.checkNotNull(
                context.getSystemService("serial"),
                "SerialManager no found");

        return new SerialManagerProxyImpl(manager, context, portSpeed);
    }

    @Override
    public String[] getSerialPorts() {
        return mProxy.getSerialPorts();
    }

    @Override
    public SerialPortProxy openSerialPort(String name) throws IOException {
        final SerialPort serialPort = Preconditions.checkNotNull(mProxy.openSerialPort(name, mPortSpeed));
        return new SerialPortProxyImpl(serialPort);
    }

    @Override
    public void acquireWakeLock() {
        wakeLock.acquire(500000);
    }

    @Override
    public void releaseWakeLock() {
        wakeLock.release();
    }
}
