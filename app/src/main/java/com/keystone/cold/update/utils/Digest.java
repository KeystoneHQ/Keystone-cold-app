/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package com.keystone.cold.update.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum Digest {
    MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256");

    private static final String TAG = "Vault.Digest";

    private final String name;

    Digest(String name) {
        this.name = name;
    }

    @Nullable
    public byte[] checksum(@NonNull File file) {
        if (!file.exists()) {
            Log.e(TAG, "file is not exist");
            return null;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            return checksum(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public byte[] checksum(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        } else {
            try (InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
                return checksum(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Nullable
    public byte[] checksum(@NonNull InputStream inputStream) throws IOException {
        final MessageDigest digest;

        try {
            digest = MessageDigest.getInstance(this.name);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        final byte[] buffer = new byte[1024];
        int read;

        while ((read = inputStream.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }

        return digest.digest();
    }
}
