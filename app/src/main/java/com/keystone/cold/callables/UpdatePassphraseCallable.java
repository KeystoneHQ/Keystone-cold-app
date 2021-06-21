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

import android.text.TextUtils;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class UpdatePassphraseCallable implements Callable<Boolean> {

    private final String passphrase;
    private final String password;
    private final String signature;

    public UpdatePassphraseCallable(String passphrase, String password, String signature) {
        this.passphrase = passphrase;
        this.password = password;
        this.signature = signature;
    }

    @Override
    public Boolean call() {
        if (TextUtils.isEmpty(signature) && TextUtils.isEmpty(password)) {
            return false;
        }
        try {
            final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.UPDATE_PASSPHRASE)
                    .addTextPayload(CONSTANTS.TAGS.PASSPHRASE, passphrase);

            if (!TextUtils.isEmpty(password)) {
                builder.addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password);
            } else {
                builder.addHexPayload(CONSTANTS.TAGS.MESSAGE_SIGNATURE, signature);
            }
            final Callable<Packet> callable = new BlockingCallable(builder.build());
            callable.call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}