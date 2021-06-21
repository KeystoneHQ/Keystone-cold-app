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

import com.keystone.coinlib.MnemonicUtils;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class WriteMnemonicCallable implements Callable<Boolean> {

    private final String mnemonic;
    private final String password;
    private final byte[] slip39MasterSeed;
    private final int slip39Id;
    private final int slip39Exponent;

    public WriteMnemonicCallable(@NonNull String mnemonic, @NonNull String password) {
        this.mnemonic = mnemonic;
        this.password = password;
        this.slip39MasterSeed = null;
        this.slip39Id = 0;
        this.slip39Exponent = 0;
    }

    public WriteMnemonicCallable(@NonNull byte[] slip39MasterSeed,
                                 int slip39Id,
                                 int slip39Exponent,
                                 @NonNull String password) {
        this.mnemonic = null;
        this.password = password;
        this.slip39MasterSeed = slip39MasterSeed;
        this.slip39Id = slip39Id;
        this.slip39Exponent = slip39Exponent;
    }

    @Override
    public Boolean call() {
        try {
            final Packet.Builder builder = new Packet.Builder(CONSTANTS.METHODS.WRITE_MNEMONIC);
            if (!TextUtils.isEmpty(mnemonic)) {
                builder.addTextPayload(CONSTANTS.TAGS.MNEMONIC, mnemonic)
                        .addBytesPayload(CONSTANTS.TAGS.ENTROPY, MnemonicUtils.generateEntropy(mnemonic));
            } else if(slip39MasterSeed != null) {
                builder.addBytesPayload(CONSTANTS.TAGS.SLIP39_MASTER_SEED, slip39MasterSeed)
                        .addShortPayload(CONSTANTS.TAGS.SLIP39_ID, slip39Id)
                        .addBytePayload(CONSTANTS.TAGS.SLIP39_EXPONENT, slip39Exponent);
            }

            builder.addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
            .build();
            final Callable<Packet> callable = new BlockingCallable(builder.build());
            callable.call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}