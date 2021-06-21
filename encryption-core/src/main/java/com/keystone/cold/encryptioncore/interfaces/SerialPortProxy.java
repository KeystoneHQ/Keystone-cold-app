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

package com.keystone.cold.encryptioncore.interfaces;

import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SerialPortProxy {
    void open(ParcelFileDescriptor pfd, int speed) throws IOException;

    void close() throws IOException;

    int read(ByteBuffer buffer, int offset) throws IOException;

    void write(ByteBuffer buffer, int length) throws IOException;

    void sendBreak();
}