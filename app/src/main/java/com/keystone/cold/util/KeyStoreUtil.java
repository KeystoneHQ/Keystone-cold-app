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

package com.keystone.cold.util;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.keystone.coinlib.Util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;


public class KeyStoreUtil {

    public static final String KEYSTORE_PROVIDER_NAME = "AndroidKeyStore";
    public static final String KEY_ALIAS = "vault_key_alias";
    private static final String CIPHER = KeyProperties.KEY_ALGORITHM_AES+"/"
            + KeyProperties.BLOCK_MODE_GCM+"/"
            + KeyProperties.ENCRYPTION_PADDING_NONE;

    public KeyStoreUtil() {

    }

    public Cipher prepareCipher() {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        return cipher;
    }

    /**
     * PrepareKeyStore to get cryptographic from Keystore
     *
     * @return keystore
     */
    public static KeyStore prepareKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_NAME);
            ks.load(null);
            return ks;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(final byte[] data) {
        return this.encrypt(KEY_ALIAS, data);
    }

    public byte[] encrypt(final String alias, final byte[] data) {
        final KeyStore ks = prepareKeyStore();
        final Cipher cipher = prepareCipher();

        try {
            if (ks.getEntry(alias, null) == null) {
                generateKeyInKeyStore(alias);
            }
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        try {
            final Key key = ks.getKey(alias, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            byte[] res = cipher.doFinal(data);
            return Util.concat(iv, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(final byte[] data) {
        return this.decrypt(KEY_ALIAS, data);
    }

    public byte[] decrypt(final String alias, final byte[] data) {
        final Cipher cipher = prepareCipher();
        final KeyStore ks = prepareKeyStore();

        try {
            final Key key = ks.getKey(alias, null);
            //Check whether encryptionIv is not null
            byte[] encryptedIV = Arrays.copyOfRange(data, 0, 12);
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptedIV);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] encryptedData = Arrays.copyOfRange(data, 12, data.length);
            return cipher.doFinal(encryptedData);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("key is invalid.");
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | BadPaddingException
                | KeyStoreException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }


    private void generateKeyInKeyStore(final String alias) {
        generateKeyInKeyStore(alias,false);
    }

    public void generateKeyInKeyStore(final String alias, boolean authenticationRequired) {
        try {
            final KeyGenerator keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER_NAME);

            final KeyGenParameterSpec keySpec;

            keySpec = new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(authenticationRequired)
                    .build();

            try {
                keyGenerator.init(keySpec);
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            }

            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateRSAKeyInKeyStore(final String alias, boolean authenticationRequired) {
        try {
            final KeyPairGenerator keyGenerator =
                    KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER_NAME);

            final KeyGenParameterSpec keySpec;

            keySpec = new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setUserAuthenticationRequired(authenticationRequired)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .build();

            try {
                keyGenerator.initialize(keySpec);
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            }

            keyGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateSecp256r1InKeyStore(final String alias, boolean authenticationRequired) {
        try {
            final KeyPairGenerator keyGenerator =
                    KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER_NAME);

            final KeyGenParameterSpec keySpec;

            keySpec = new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setUserAuthenticationRequired(authenticationRequired)
                    .build();

            try {
                keyGenerator.initialize(keySpec);
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            }

            keyGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeAllKey(Context context) {
        KeyStore ks = prepareKeyStore();
        try {
            List<String> aliases = Collections.list(ks.aliases());
            for (String alias : aliases) {
                ks.deleteEntry(alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }
}