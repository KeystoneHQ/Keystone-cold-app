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

import android.util.Log;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import static com.keystone.cold.callables.UpdateCallable.TYPE_PACKAGE_HEADER;
import static com.keystone.cold.callables.UpdateCallable.calculatePackageType;
import static com.keystone.cold.callables.UpdateCallable.fitBuffer;

public class RequestUpdateCallable implements Callable<Boolean> {
    private final byte[] updateData;
    private final String password;
    private static final int UPDATE_RETRY_TIMES = 3;
    private static final int PACKET_RETRY_TIMES = 6;
    private static final int METADATA_SIZE = 128;
    private String TAG = "RequestUpdateCallableNew";

    public RequestUpdateCallable(byte[] updateData, String password) {
        this.updateData = updateData;
        this.password = password;

    }

    @Override
    public Boolean call() {
        for (int i = 1; ; ++i) {
            boolean updateSuccess = true;

            try {
                updating();
            } catch (Exception e) {
                e.printStackTrace();
                updateSuccess = false;
            }

            if (updateSuccess) {
                break;
            } else if (i == UPDATE_RETRY_TIMES) {
                return false;
            }
        }
        return true;
    }

    private boolean updating() throws Exception {
        final byte[] readBuffer = new byte[CONSTANTS.CONFIG.PAGE_SIZE];
        try (InputStream inputStream = new ByteArrayInputStream(updateData)) {
            // skip metadata
            byte[] metaData = new byte[METADATA_SIZE];
            inputStream.read(metaData);

            int read;
            final int packageSize = updateData.length - METADATA_SIZE;
            int packageIndex = 0;
            int readPosition = 0;

            while ((read = inputStream.read(readBuffer)) > 0) {
                final byte[] availableBuffer = fitBuffer(readBuffer, read);
                final int type = calculatePackageType(packageSize, readPosition, read);
                for (int i = 1; i <= PACKET_RETRY_TIMES; ++i) {
                    boolean writeSuccess = true;
                    final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.REQUEST_UPDATE)
                            .addBytePayload(CONSTANTS.TAGS.UPDATING_PACKAGE_TYPE, type)
                            .addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                            .addBytesPayload(CONSTANTS.TAGS.UPDATING_PACKAGE, availableBuffer);
                    final Callable callable = new BlockingCallable(builder.build());
                    try {
                        Log.w(TAG, String.format("write the %sth package", packageIndex));
                        callable.call();
                    } catch (Exception e) {
                        Log.e(TAG, "updating: ", e);
                        writeSuccess = false;
                    }

                    if (writeSuccess) {
                        break;
                    } else if (i == PACKET_RETRY_TIMES) {
                        throw new RuntimeException("write updating packet out of retry times");
                    }
                }
                readPosition += read;
                ++packageIndex;
            }
            for (int i = 1; i <= PACKET_RETRY_TIMES; ++i) {
                boolean writeSuccess = true;
                final Packet.Builder builder = new Packet.Builder(0x0203)
                        .addBytePayload(CONSTANTS.TAGS.UPDATING_PACKAGE_TYPE, TYPE_PACKAGE_HEADER)
                        .addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                        .addBytesPayload(CONSTANTS.TAGS.UPDATING_PACKAGE, metaData);
                final Callable callable = new BlockingCallable(builder.build());
                try {
                    Log.w(TAG, String.format("write the header package", packageIndex));
                    callable.call();
                } catch (Exception e) {
                    Log.e(TAG, "updating: ", e);
                    writeSuccess = false;
                }

                if (writeSuccess) {
                    break;
                } else if (i == PACKET_RETRY_TIMES) {
                    throw new RuntimeException("write updating packet out of retry times");
                }
            }
        }

        return true;

    }
}
