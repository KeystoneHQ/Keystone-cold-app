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

package com.keystone.cold.update;

import android.content.Context;
import android.os.Looper;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.BuildConfig;
import com.keystone.cold.callables.UpdateCallable;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;
import com.keystone.cold.silentInstall.SilentInstallUtils;
import com.keystone.cold.silentInstall.listener.InstallResultListener;
import com.keystone.cold.update.data.FileInfo;
import com.keystone.cold.update.data.UpdateManifest;
import com.keystone.cold.update.utils.Digest;
import com.keystone.cold.update.utils.FileUtils;
import com.keystone.cold.update.utils.Storage;
import com.keystone.cold.update.utils.ZipHelper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class Updating implements Callable<Boolean> {
    private static final String TAG = "Vault.Updating";
    private final UpdateManifest mManifest;
    private final Storage mStorage;
    private final Context mContext;
    private final String password;

    public Updating(@NonNull Storage storage, Context context, @NonNull UpdateManifest manifest, String password) {
        mStorage = storage;
        mContext = context;
        mManifest = Preconditions.checkNotNull(manifest);
        this.password = password;
    }

    @Nullable
    private static File extractAndCheck(@NonNull ZipFile zipFile, @NonNull String key, @NonNull FileInfo info, @NonNull File dir) {
        if (ZipHelper.checkFileHeader(zipFile, info.fileName)) {
            final File file;

            try {
                file = ZipHelper.extract(zipFile, info.fileName, key, dir.getAbsolutePath());

                if (file.exists()) {
                    final String md5 = ByteFormatter.bytes2hex(Digest.MD5.checksum(file));
                    final String sha1 = ByteFormatter.bytes2hex(Digest.SHA1.checksum(file));

                    if (Objects.equals(md5, info.md5) && Objects.equals(sha1, info.sha1)) {
                        return file;
                    }
                }
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private boolean update(@NonNull ZipFile zipFile, @NonNull String key, @NonNull UpdateManifest manifest, @NonNull File target) {
        boolean result = true;

        if (manifest.serial != null) {
            Log.i(TAG, "updating serial");
            result = updateSerial(zipFile, key, manifest.serial, target);
            Log.i(TAG, "update serial success :" + result);
        }

        if (!result) {
            return false;
        }

        if (manifest.app != null) {
            Log.i(TAG, "updating app");
            result = updateApp(zipFile, key, manifest.app, target);
            Log.i(TAG, "update app success :" + result);
        }

        if (!result) {
            return false;
        }

        if (manifest.system != null) {
            Log.i(TAG, "updating system");
            result = updateSystem(zipFile, key, manifest.system, target);
            Log.i(TAG, "update system success :" + result);
        }

        return result;
    }

    private boolean updateSerial(@NonNull ZipFile zipFile, @NonNull String key, @NonNull FileInfo serial, File dir) {
        final File file = extractAndCheck(zipFile, key, serial, dir);

        if (file == null || !file.exists()) {
            return false;
        }

        final byte[] data = FileUtils.bufferlize(file);
        file.delete();

        if (data == null || data.length == 0) {
            return false;
        }

        final Callable callable = new UpdateCallable(data, password);

        try {
            callable.call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateApp(@NonNull ZipFile zipFile, @NonNull String key, @NonNull FileInfo app, File dir) {
        final File file = extractAndCheck(zipFile, key, app, dir);

        if (file == null || !file.exists()) {
            return false;
        }
        CountDownLatch countDown = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
        SilentInstallUtils.runReplaceInstall(mContext, file.getPath(), new InstallResultListener() {
            @Override
            public void installSuccess(String packageName) {
                result[0] = true;
                countDown.countDown();
            }

            @Override
            public void installFailure(String packageName) {
                result[0] = false;
                countDown.countDown();
            }
        });

        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    private boolean updateSystem(@NonNull ZipFile zipFile, @NonNull String key, @NonNull FileInfo system, File target) {
        final File file = extractAndCheck(zipFile, key, system, target);

        if (file == null || !file.exists()) {
            return false;
        }

        Looper.getMainLooper().getQueue().addIdleHandler(() -> {
            try {
                RecoverySystem.installPackage(mContext, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });

        return true;
    }

    @Override
    public Boolean call() throws Exception {
        String updateKey = BuildConfig.UPDATE_PUBLIC_KEY;

        if (TextUtils.isEmpty(updateKey)) {
            return false;
        }

        final File updateFile = mStorage.getUpdateZipFile();

        if (!updateFile.exists()) {
            return false;
        }

        final ZipFile zipFile = ZipHelper.wrapUpdateZipFile(updateFile);
        return zipFile != null && update(zipFile, updateKey, mManifest, mStorage.getUpdateCacheDir());
    }
}
