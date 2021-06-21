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

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

public class VerifyFingerprintCallable implements Callable<String> {

    private final String signature;

    public VerifyFingerprintCallable(@NonNull String signature) {
        this.signature = signature;
    }

    @Override
    public String call() {
        final Callable<Packet> callable = new BlockingCallable(
                new Packet.Builder(CONSTANTS.METHODS.VERIFY_FINGERPRINT)
                        .addHexPayload(CONSTANTS.TAGS.MESSAGE_SIGNATURE, signature)
                        .addBytePayload(CONSTANTS.TAGS.NEED_TOKEN, 1).build());
        try {
            Packet packet = callable.call();
            Payload payload = packet.getPayload(CONSTANTS.TAGS.AUTH_TOKEN);
            if (payload != null) {
                return payload.toHex();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
