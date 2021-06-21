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

package com.keystone.cold.encryptioncore.base;


import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Packet {
    private final int id;
    private final int retryTimes;
    private final int timeout;
    @NonNull
    private final SparseArrayCompat<Payload> payloads;

    private Packet(int id, @NonNull SparseArrayCompat<Payload> payloads,
                   int retryTimes,
                   int timeout) {
        this.id = id;
        this.payloads = payloads;
        this.retryTimes = retryTimes;
        this.timeout = timeout;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public SparseArrayCompat<Payload> getPayloads() {
        return payloads;
    }

    @Nullable
    public Payload getPayload(int payloadId) {
        return payloads.get(payloadId, null);
    }

    @IntRange(from = 0)
    public int getRetryTimes() {
        return retryTimes;
    }

    @IntRange(from = 0)
    public int getTimeout() {
        return timeout;
    }

    @NonNull
    @Override
    public String toString() {
        final JSONObject object = new JSONObject();

        try {
            object.put("id", ByteFormatter.addHexPrefix(id));

            final JSONArray array = new JSONArray();
            for (int i = 0, size = payloads.size(); i < size; ++i) {
                final int tag = payloads.keyAt(i);
                final Payload payload = payloads.valueAt(i);
                if (payload == null) {
                    continue;
                }


                final JSONObject payloadObj = new JSONObject();
                payloadObj.put("tag", "0x" + String.format("%04x", tag));
                payloadObj.put("value", ByteFormatter.bytes2hex(payload.getValue()));

                array.put(payloadObj);
            }

            object.put("payloads", array);
            return object.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static class Builder {
        private final int id;
        private final SparseArrayCompat<Payload> payloads;
        private int retryTimes;
        private int timeout;

        public Builder(int id) {
            this.id = id;
            this.retryTimes = 0;
            this.payloads = new SparseArrayCompat<>();
        }

        public Builder setRetryTimes(@IntRange(from = 0) int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder setTimeout(@IntRange(from = 0) int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder addBytePayload(int payloadId, int i) {
            final byte[] value = new byte[]{(byte) i};
            payloads.append(payloadId, new Payload(value));

            return this;
        }

        public Builder addShortPayload(int payloadId, int i) {
            final byte[] value = ByteFormatter.short2bytes(i);
            payloads.append(payloadId, new Payload(value));

            return this;
        }

        public Builder addIntPayload(int payloadId, int i) {
            final byte[] value = ByteFormatter.int2bytes(i);
            payloads.append(payloadId, new Payload(value));

            return this;
        }

        public Builder addHexPayload(int payloadId, @NonNull String hex) {
            Preconditions.checkNotNull(hex);
            final byte[] value = ByteFormatter.hex2bytes(hex);
            payloads.append(payloadId, new Payload(value));

            return this;
        }

        public Builder addBytesPayload(int payloadId, byte[] bytes) {
            Preconditions.checkNotNull(bytes);
            payloads.append(payloadId, new Payload(bytes));

            return this;
        }

        public Builder addTextPayload(int payloadId, @NonNull String text) {
            Preconditions.checkNotNull(text);
            final byte[] value = ByteFormatter.utf82bytes(text);
            payloads.append(payloadId, new Payload(value));

            return this;
        }

        public Packet build() {
            return new Packet(id, payloads, retryTimes, timeout);
        }
    }
}
