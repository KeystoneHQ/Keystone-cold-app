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

package com.keystone.cold.encryptioncore.cipher;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.interfaces.Cipher;
import com.keystone.cold.encryptioncore.utils.Preconditions;

public class CipherImpl implements Cipher {
    private final byte[] mKey;
    private final byte[] mKeyIv;

    public CipherImpl(@NonNull byte[] key, @NonNull byte[] keyIv) {
        mKey = Preconditions.checkNotNull(key);
        mKeyIv = Preconditions.checkNotNull(keyIv);
    }

    @Override
    public byte[] encrypt(@NonNull byte[] data) throws Exception {
        Preconditions.checkNotNull(data);
        return DES.encrypt(mKey, mKeyIv, data);
    }

    @Override
    public byte[] decrypt(@NonNull byte[] encrypted) throws Exception {
        Preconditions.checkNotNull(encrypted);
        return DES.decrypt(mKey, mKeyIv, encrypted);
    }
}
