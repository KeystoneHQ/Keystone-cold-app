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

package com.keystone.cold.update.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.UUID;

public class ZipHelper {
    public static boolean checkFileHeader(@NonNull ZipFile zipFile, @NonNull String fileName) {
        try {
            return zipFile.getFileHeader(fileName) != null;
        } catch (ZipException e) {
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    public static File extract(@NonNull ZipFile zipFile, @NonNull String fileName,
                               @NonNull String password, @NonNull String destPath) throws ZipException {
        if (zipFile.isEncrypted() || !TextUtils.isEmpty(fileName)) {
            zipFile.setPassword(password);
        }

        final String randomName = UUID.randomUUID().toString();

        zipFile.extractFile(fileName, destPath, null, randomName);
        return new File(destPath, randomName);
    }

    @Nullable
    public static ZipFile wrapUpdateZipFile(@NonNull File file) throws ZipException {
        final ZipFile zipFile = new ZipFile(file);

        if (zipFile.isValidZipFile()) {
            return zipFile;
        } else {
            return null;
        }
    }
}
