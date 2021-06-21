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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.keystone.cold.encryptioncore.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.interfaces.SerialPortProxy;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import static com.keystone.cold.encryptioncore.interfaces.CONSTANTS.STX;

class SerialReader implements Callable<byte[]> {
    private static final String TAG = "EncryptionCore.SerialReader";
    private final SerialPortProxy mPort;

    SerialReader(@NonNull SerialPortProxy port) {
        mPort = Preconditions.checkNotNull(port);
    }

    /**
     * @param buffer    readable byte buffer
     * @param milestone current milestone
     * @param position  current position
     * @return new target
     */
    @IntRange(from = 0)
    @VisibleForTesting
    static int checkMilestone(@NonNull ByteBuffer buffer, int milestone, int position) {
        switch (milestone) {
            case 0: { // stx
                Preconditions.checkArgument(buffer.get(position) == CONSTANTS.STX,
                        String.format("stx must be constant %s", ByteFormatter.addHexPrefix(CONSTANTS.STX)));
                return 1; // sizeOf(encryption)
            }

            case 1: { // encryption
                final byte b = buffer.get(position);
                Preconditions.checkArgument(b == CONSTANTS.ENCRYPTION_STATUS.DISABLED
                        || b == CONSTANTS.ENCRYPTION_STATUS.ENABLED, String.format("encryption must be one of [%s, %s]",
                        ByteFormatter.addHexPrefix(CONSTANTS.ENCRYPTION_STATUS.DISABLED),
                        ByteFormatter.addHexPrefix(CONSTANTS.ENCRYPTION_STATUS.ENABLED)));
                return 2; // sizeOf(lenOfBody)
            }

            case 2: { // the length of body
                final int length = buffer.getShort(position);
                Preconditions.checkArgument(length > 0, "the length of body must be positive");

                return length; // sizeOf(len)
            }

            case 3: {
                return 1; // sizeOf(etx)
            }

            case 4: {
                Preconditions.checkArgument(buffer.get(position) == CONSTANTS.ETX, String.format("etx must be constant %s",
                        ByteFormatter.addHexPrefix(CONSTANTS.ETX)));

                return 1; // sizeOf(lrc)
            }

            case 5: {
                return 0; // completed
            }

            default:
                throw new IllegalArgumentException("illegal milestone");
        }
    }

    @Override
    public byte[] call() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(CONSTANTS.MAX_BUFFER_SIZE);

        boolean looping = true;
        int position = 0;
        int limit = 0;
        int target = 1; // sizeOf(stx)
        int milestone = 0;
        int read;

        while (looping) {
            read = mPort.read(buffer, limit);
            if (read != 0) {
                byte[] data = new byte[read];
                int pos = buffer.position();
                buffer.get(data);
                buffer.position(pos);
                if (buffer.get(0) != STX) {
                    Log.w(TAG, "receive: " + ByteFormatter.bytes2utf8(data));
                    buffer.position(0);
                    continue;
                }
            }

            limit += read;
            while (limit - position >= target) {
                final int newTarget = checkMilestone(buffer, milestone, position);

                if (newTarget == 0) {
                    looping = false;
                    position += 1;
                    break;
                }
                ++milestone;
                position += target;
                target = newTarget;
            }
        }

        buffer.position(position);
        buffer.flip();
        final byte[] bytes = new byte[position];
        buffer.get(bytes);

        return bytes;
    }
}
