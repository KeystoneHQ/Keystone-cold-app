/*
 *
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
 *
 */

package com.keystone.cold.callables;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

public class FingerprintPolicyCallable implements Callable<Boolean> {

    public static int TYPE_SIGN_TX = 0;
    public static int TYPE_PASSPHRASE = 1;
    public static int WRITE = 0;
    public static int READ = 1;
    public static int ON = 1;
    public static int OFF = 0;
    private String password;
    private final int type;
    private int value;
    private int accessMode; //1 read, 0 write

    public FingerprintPolicyCallable(String password, int accessMode, int type, int value) {
        this.password = password;
        this.accessMode = accessMode;
        this.type = type;
        this.value = value;
    }

    public FingerprintPolicyCallable(int accessMode, int type) {
        this.accessMode = accessMode;
        this.type = type;
    }

    @Override
    public Boolean call() {
        try {
            final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.FINGERPRINT_POLICY);
            builder.addBytePayload(CONSTANTS.TAGS.ACCESS_MODE, accessMode)
                    .addBytePayload(CONSTANTS.TAGS.FINGERPRINT_POLICY_TYPE, type)
                    .build();
            if (accessMode == WRITE) {
                builder.addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                        .addBytePayload(CONSTANTS.TAGS.FINGERPRINT_POLICY_VALUE, value);
            }

            final Callable<Packet> callable = new BlockingCallable(builder.build());
            if (accessMode == READ) {
                Packet result = callable.call();
                Payload payload = result.getPayload(CONSTANTS.TAGS.FINGERPRINT_POLICY_VALUE);
                if (payload != null) {
                    return payload.toInt() == 1;
                }
                return false;
            } else {
                callable.call();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}