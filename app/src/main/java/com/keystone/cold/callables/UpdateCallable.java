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

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class UpdateCallable implements Callable<Void> {
    private static final String TAG = "Vault.UpdateCallable";
    private static final int UPDATE_RETRY_TIMES = 3;
    private static final int PACKET_RETRY_TIMES = 6;
    private static final int TYPE_PACKAGE_START = 0x00;
    private static final int TYPE_PACKAGE_MIDDLE = 0x01;
    static final int TYPE_PACKAGE_END = 0x02;
    static final int TYPE_PACKAGE_HEADER = 0x03;

    private static final int APP_BOOT_SWITCH_TIME = 5000; //ms
    private static final int METADATA_SIZE = 128; // 64byte meta data


    private final byte[] mUpdateData;
    private String password;

    public UpdateCallable(@NonNull byte[] updateData, String password) {
        mUpdateData = updateData;
        this.password = password;
    }

    @NonNull
    static byte[] calculateChecksum(@NonNull byte[] data) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            return Preconditions.checkNotNull(CONSTANTS.CONFIG.DIGEST.checksum(inputStream));
        }
    }

    static byte[] fitBuffer(@NonNull byte[] data, int size) {
        if (data.length == size) {
            return data;
        }

        final byte[] result = new byte[size];
        System.arraycopy(data, 0, result, 0, size);
        return result;
    }

    static int calculatePackageType(int dataLength, int currentPosition, int read) {
        if (currentPosition == 0) {
            return TYPE_PACKAGE_START;
        } else if (currentPosition + read == dataLength) {
            return TYPE_PACKAGE_END;
        } else {
            return TYPE_PACKAGE_MIDDLE;
        }
    }

    @Override
    public Void call() throws Exception {

        if (!prepareUpdate()) return null;

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
                throw new RuntimeException("update serial out of retry times");
            }
        }

        return null;
    }

    private void updating() throws Exception {
        final byte[] readBuffer = new byte[CONSTANTS.CONFIG.PAGE_SIZE];

        try (InputStream inputStream = new ByteArrayInputStream(mUpdateData)) {
            byte[] header = new byte[METADATA_SIZE];
            // skip metadata
            inputStream.read(header);

            int read;
            final int packageSize = mUpdateData.length - METADATA_SIZE;
            int packageIndex = 0;
            int readPosition = 0;

            while ((read = inputStream.read(readBuffer)) > 0) {
                final byte[] availableBuffer = fitBuffer(readBuffer, read);
                final int type = calculatePackageType(packageSize, readPosition, read);
                for (int i = 1; i <= PACKET_RETRY_TIMES; ++i) {
                    if (writeUpdateBytes(packageIndex, availableBuffer, type, i)) break;
                }
                readPosition += read;
                ++packageIndex;
            }
            for (int i = 1; i <= PACKET_RETRY_TIMES; ++i) {
                if (writeHeaderBytes(header)) break;
            }
        }

        Thread.sleep(APP_BOOT_SWITCH_TIME);
        Preconditions.checkState(!new CheckBootModeCallable().call(),
                "update fail, se has still stayed in boot mode");
    }

    private boolean writeHeaderBytes(byte[] header) {
        final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.WRITE_UPDATE_BYTES)
                .addBytePayload(CONSTANTS.TAGS.UPDATING_PACKAGE_TYPE, TYPE_PACKAGE_HEADER)
                .addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                .addBytesPayload(CONSTANTS.TAGS.UPDATING_PACKAGE, header);
        final Callable callable = new BlockingCallable(builder.build());
        try {
            callable.call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean writeUpdateBytes(int packageIndex, byte[] availableBuffer, int type, int i) {
        boolean writeSuccess = true;
        final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.WRITE_UPDATE_BYTES)
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
            return true;
        } else if (i == PACKET_RETRY_TIMES) {
            throw new RuntimeException("write updating packet out of retry times");
        }
        return false;
    }

    private boolean prepareUpdate() throws Exception {
        int retry = 0;

        while (!checkBootMode()) {
            if (retry >= 10) {
                return false;
            }
            if (!requestUpdate()) {
                return false;
            }
            Thread.sleep(APP_BOOT_SWITCH_TIME);
            retry++;
        }

        return true;
    }

    private boolean checkBootMode() {
        return new CheckBootModeCallable().call();
    }

    private boolean requestUpdate() {
        return new RequestUpdateCallable(mUpdateData, password).call();
    }
}