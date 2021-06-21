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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.collection.SparseArrayCompat;

import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;
import com.keystone.cold.encryptioncore.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.interfaces.Cipher;
import com.keystone.cold.encryptioncore.interfaces.Packer;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

class PackerImpl implements Packer {
    private final Cipher mCipher;

    PackerImpl(@Nullable Cipher cipher) {
        mCipher = cipher;
    }

    @Override
    public byte[] serialize(@NonNull Packet packet) throws Exception {
        Preconditions.checkNotNull(packet);
        byte[] body = serializeBody(packet);
        body = mCipher != null ? mCipher.encrypt(body) : body;

        if (body.length > CONSTANTS.MAX_BODY_SIZE) {
            throw new IllegalArgumentException(String.format("the length of packet body [%s] is greater than %s",
                    body.length, CONSTANTS.MAX_BODY_SIZE));
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(CONSTANTS.STX);
        outputStream.write(mCipher != null ?
                CONSTANTS.ENCRYPTION_STATUS.ENABLED : CONSTANTS.ENCRYPTION_STATUS.DISABLED
        );
        outputStream.write(ByteFormatter.short2bytes(body.length));
        outputStream.write(body);
        outputStream.write(CONSTANTS.ETX);

        final byte lrc = calculateLRC(outputStream.toByteArray());
        outputStream.write(lrc);

        return outputStream.toByteArray();
    }

    @NonNull
    @VisibleForTesting
    byte[] serializeBody(@NonNull Packet packet) throws IOException {
        fitPayload(packet, CONSTANTS.TAGS.PACKET_ID, ByteFormatter.short2bytes(packet.getId()));
        return serializePayloads(packet.getPayloads());
    }

    private void fitPayload(@NonNull Packet packet, int id, @NonNull byte[] bytes) {
        if (packet.getPayload(id) == null) {
            packet.getPayloads().put(id, new Payload(bytes));
        }
    }

    @NonNull
    @VisibleForTesting
    byte[] serializePayloads(@NonNull SparseArrayCompat<Payload> payloads) throws
            IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0, size = payloads.size(); i < size; ++i) {
            final int tag = payloads.keyAt(i);
            final Payload payload = payloads.valueAt(i);

            if (payload == null) {
                continue;
            }

            outputStream.write(serializeTLV(tag, payload.getValue()));
        }

        return outputStream.toByteArray();
    }

    @NonNull
    @VisibleForTesting
    byte[] serializeTLV(int tag, @NonNull byte[] value) {
        final byte[] bytes = new byte[2 + 2 + value.length];
        System.arraycopy(ByteFormatter.short2bytes(tag), 0, bytes, 0, 2);
        System.arraycopy(ByteFormatter.short2bytes(value.length), 0, bytes, 2, 2);
        System.arraycopy(value, 0, bytes, 4, value.length);

        return bytes;
    }

    @Override
    public Packet deserialize(@NonNull byte[] bytes) throws Exception {
        Preconditions.checkNotNull(bytes);
        checkSerializedData(bytes);

        final boolean encrypt = bytes[1] == CONSTANTS.ENCRYPTION_STATUS.ENABLED;
        Preconditions.checkState(!(encrypt && mCipher == null), "encrypt disabled but got encrypted data");
        final int lenOfBody = ByteFormatter.bytes2short(new byte[]{bytes[2], bytes[3]});
        byte[] body = Arrays.copyOfRange(bytes, 4, 4 + lenOfBody);
        body = encrypt ? mCipher.decrypt(body) : body;

        final Packet.Builder builder = deserializeBody(body);

        return builder.build();
    }

    @NonNull
    @VisibleForTesting
    Packet.Builder deserializeBody(@NonNull byte[] bytes) {
        final SparseArrayCompat<Payload> payloads = deserializePayloads(bytes);

        Preconditions.checkState(payloads.indexOfKey(CONSTANTS.TAGS.PACKET_ID) >= 0,
                "deserialize data has no id which tag is %s");

        final int packetId = Preconditions.checkNotNull(payloads.get(CONSTANTS.TAGS.PACKET_ID)).toInt();
        final Packet.Builder builder = new Packet.Builder(packetId);

        for (int i = 0, size = payloads.size(); i < size; ++i) {
            final int tag = payloads.keyAt(i);
            final Payload payload = Preconditions.checkNotNull(payloads.valueAt(i));

            builder.addBytesPayload(tag, payload.getValue());
        }

        return builder;
    }

    @NonNull
    @VisibleForTesting
    SparseArrayCompat<Payload> deserializePayloads(@NonNull byte[] bytes) {
        final SparseArrayCompat<Payload> payloads = new SparseArrayCompat<>();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        inputStream.reset();

        int offset = 0;
        int available = inputStream.available();

        while (offset < available) {
            final byte[] twoBytesBuffer = new byte[2];
            inputStream.read(twoBytesBuffer, 0, 2);
            offset += 2;
            final int tag = ByteFormatter.bytes2short(twoBytesBuffer);

            inputStream.read(twoBytesBuffer, 0, 2);
            offset += 2;
            final int lenOfVal = ByteFormatter.bytes2short(twoBytesBuffer);

            final byte[] valueBuffer = new byte[lenOfVal];
            inputStream.read(valueBuffer, 0, lenOfVal);
            offset += lenOfVal;
            payloads.put(tag, new Payload(valueBuffer));
        }

        Preconditions.checkState(offset == available, "illegal data");
        return payloads;
    }

    @VisibleForTesting
    void checkSerializedData(@NonNull byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        Preconditions.checkArgument(bytes.length >= CONSTANTS.MIN_BUFFER_SIZE,
                String.format("the length of bytes must be greater or equal to %s",
                        CONSTANTS.MIN_BUFFER_SIZE));

        Preconditions.checkArgument(bytes[0] == CONSTANTS.STX,
                String.format("the first byte must be constant %s",
                        ByteFormatter.addHexPrefix(CONSTANTS.STX)));

        Preconditions.checkArgument(bytes[1] == CONSTANTS.ENCRYPTION_STATUS.DISABLED
                        || bytes[1] == CONSTANTS.ENCRYPTION_STATUS.ENABLED,
                String.format("the second byte must be one of [%s, %s]",
                        ByteFormatter.addHexPrefix(CONSTANTS.ENCRYPTION_STATUS.DISABLED),
                        ByteFormatter.addHexPrefix(CONSTANTS.ENCRYPTION_STATUS.ENABLED)));

        Preconditions.checkArgument(bytes[bytes.length - 2] == CONSTANTS.ETX,
                String.format("the second to last byte must be constant %s",
                        ByteFormatter.addHexPrefix(CONSTANTS.ETX)));

        Preconditions.checkArgument(bytes[bytes.length - 1] ==
                        calculateLRC(Arrays.copyOf(bytes, bytes.length - 1)),
                String.format("lrc is not the same, expected %s, but actual is %s",
                        bytes[bytes.length - 1],
                        calculateLRC(Arrays.copyOf(bytes, bytes.length - 1))));
    }

    @VisibleForTesting
    byte calculateLRC(@NonNull byte[] bytes) {
        byte lrc = 0x0;

        for (byte b : bytes) {
            lrc ^= b;
        }

        return lrc;
    }
}
