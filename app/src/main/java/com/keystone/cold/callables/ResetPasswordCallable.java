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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class ResetPasswordCallable implements Callable<Boolean> {

    private final String newPassword;
    private final String mnemonic;
    private final String slip39MasterSeed;
    private final int slip39Id;

    public ResetPasswordCallable(@NonNull String newPassword, @Nullable String mnemonic,
                                 @Nullable String slip39MasterSeed,
                                 int slip39Id) {
        this.newPassword = newPassword;
        this.mnemonic = mnemonic;
        this.slip39MasterSeed = slip39MasterSeed;
        this.slip39Id = slip39Id;
    }

    @Override
    public Boolean call() {
        Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.RESET_USER_PASSWORD)
                .addHexPayload(CONSTANTS.TAGS.NEW_PASSWORD, newPassword);
        if (!TextUtils.isEmpty(mnemonic)) {
            builder.addTextPayload(CONSTANTS.TAGS.MNEMONIC, mnemonic);
        } else if (!TextUtils.isEmpty(slip39MasterSeed)) {
            builder.addHexPayload(CONSTANTS.TAGS.SLIP39_MASTER_SEED, slip39MasterSeed)
                    .addShortPayload(CONSTANTS.TAGS.SLIP39_ID, slip39Id);
        }

        final Callable<Packet> callable = new BlockingCallable(builder.build());
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
