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

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class WebAuthCallable implements Callable<String> {

    private byte[] encrypted;
    private byte[] signature;

    public WebAuthCallable(String data) {
        try {
            byte[] bytes = Base64.decode(data);
            this.encrypted = Arrays.copyOfRange(bytes, 0, bytes.length - 64);
            this.signature = Arrays.copyOfRange(bytes, bytes.length - 64, bytes.length);
        } catch (Exception e) {
            this.encrypted = null;
            this.signature = null;
        }

    }

    @Override
    public String call() {
        if (encrypted == null || signature == null) {
            return null;
        }
        final Callable<Packet> callable = new BlockingCallable(
                new Packet.Builder(CONSTANTS.METHODS.WEB_AUTH)
                        .addHexPayload(CONSTANTS.TAGS.ENCRYPTED, Hex.toHexString(encrypted))
                        .addHexPayload(CONSTANTS.TAGS.SIGNATURE, Hex.toHexString(signature)).build());
        final Packet result;
        try {
            result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.DECRYPTED);
            if (payload != null) {
                return payload.toUtf8();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
