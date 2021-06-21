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

import android.os.Environment;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class Storage {
    private static final String UPDATE_CACHE = "update_cache";
    private static final String UPDATE_ZIP_FILE = "update.zip";

    private final File mInternalDir;
    private final File mExternalDir;

    private Storage(@NonNull File internalDir, @NonNull File externalDir) {
        mInternalDir = internalDir;
        mExternalDir = externalDir;
    }

    @Nullable
    public static Storage createByEnvironment() {
        final StorageVolume[] volumes = StorageManager.getVolumeList(UserHandle.myUserId(), StorageManager.FLAG_FOR_WRITE);
        final File[] files = new File[volumes.length];
        for (int i = 0; i < volumes.length; i++) {
            files[i] = volumes[i].getPathFile();
        }

        for (File dir : files) {
            if (dir != null && dir.isDirectory()) {
                if (Environment.isExternalStorageRemovable(dir)) {
                    files[1] = dir;
                } else {
                    files[0] = dir;
                }
            }
        }

        if (files.length != 2) {
            return null;
        }
        if (files[0] == null || files[1] == null) {
            return null;
        }

        return new Storage(files[0], files[1]);
    }

    @Nullable
    public File getInternalDir() {
        return mInternalDir;
    }

    @Nullable
    public File getExternalDir() {
        return mExternalDir;
    }

    public void resetUpdateCacheDir() {
        final File dir = new File(mInternalDir, UPDATE_CACHE);

        try {
            if (dir.exists()) {
                FileUtils.deleteRecursive(dir);
            }

            dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public File getUpdateCacheDir() {
        final File dir = new File(mInternalDir, UPDATE_CACHE);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @NonNull
    public File getUpdateZipFile() {
        return new File(mExternalDir, UPDATE_ZIP_FILE);
    }

    public static boolean hasSdcard() {
        Storage storage = Storage.createByEnvironment();
        return storage != null && storage.getExternalDir() != null;
    }
}