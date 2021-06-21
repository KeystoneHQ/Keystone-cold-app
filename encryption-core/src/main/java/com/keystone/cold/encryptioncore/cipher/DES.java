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

import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

class DES {
    private static final String ALGORITHM = "desede";
    private static final String PADDING = "/CBC/PKCS5Padding";

    public static byte[] encrypt(@NonNull byte[] keyBytes, @NonNull byte[] keyIv, @NonNull byte[] data)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException,
            IllegalBlockSizeException {
        Preconditions.checkNotNull(keyBytes);
        Preconditions.checkNotNull(keyIv);
        Preconditions.checkNotNull(data);

        final DESedeKeySpec spec = new DESedeKeySpec(keyBytes);
        final Key desKey = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec);

        final Cipher cipher = Cipher.getInstance(ALGORITHM + PADDING);
        final IvParameterSpec ips = new IvParameterSpec(keyIv);
        cipher.init(Cipher.ENCRYPT_MODE, desKey, ips);

        return cipher.doFinal(data);
    }

    public static byte[] decrypt(@NonNull byte[] keyBytes, @NonNull byte[] keyIv, byte[] encrypted)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException,
            IllegalBlockSizeException {
        Preconditions.checkNotNull(keyBytes);
        Preconditions.checkNotNull(keyIv);
        Preconditions.checkNotNull(encrypted);

        final DESedeKeySpec spec = new DESedeKeySpec(keyBytes);
        final Key desKey = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec);

        final Cipher cipher = Cipher.getInstance(ALGORITHM + PADDING);
        final IvParameterSpec ips = new IvParameterSpec(keyIv);
        cipher.init(Cipher.DECRYPT_MODE, desKey, ips);

        return cipher.doFinal(encrypted);
    }
}