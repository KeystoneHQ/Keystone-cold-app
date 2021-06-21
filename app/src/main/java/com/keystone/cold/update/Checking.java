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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.BuildConfig;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.CheckBootModeCallable;
import com.keystone.cold.callables.CheckUpdateFirmwareCallable;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;
import com.keystone.cold.update.data.FileInfo;
import com.keystone.cold.update.data.UpdateManifest;
import com.keystone.cold.update.utils.FileUtils;
import com.keystone.cold.update.utils.Storage;
import com.keystone.cold.update.utils.ZipHelper;

import net.lingala.zip4j.core.ZipFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.Callable;

public class Checking implements Callable<UpdateManifest> {
    private static final String MANIFEST_FILE = "manifest.json";
    private static final String SIGNED_FILE = "signed.rsa";
    private final Storage mStorage;

    public Checking(@NonNull Storage storage) {
        mStorage = storage;
    }

    @Nullable
    private static UpdateManifest exportManifest(@NonNull ZipFile zipFile, @NonNull String key, @NonNull File target) {
        String manifestText = null;
        File signedFile = null;
        File manifestFile = null;

        try {
            final String destPath = target.getAbsolutePath();
            signedFile = ZipHelper.extract(zipFile, SIGNED_FILE, key, destPath);
            manifestFile = ZipHelper.extract(zipFile, MANIFEST_FILE, key, destPath);

            final byte[] signedBuffer = FileUtils.bufferlize(signedFile);
            final byte[] manifestBuffer = FileUtils.bufferlize(manifestFile);
            final byte[] keyBuffer = ByteFormatter.hex2bytes(key);

            Preconditions.checkNotNull(manifestBuffer);
            Preconditions.checkNotNull(signedBuffer);

            if (check(keyBuffer, manifestBuffer, signedBuffer)) {
                manifestText = ByteFormatter.bytes2utf8(manifestBuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            manifestText = null;
        } finally {
            if (signedFile != null && signedFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                signedFile.delete();
            }

            if (manifestFile != null && manifestFile.exists()) {
                manifestFile.delete();
            }
        }

        if (TextUtils.isEmpty(manifestText)) {
            return null;
        }

        try {
            final JSONObject jsonObject = new JSONObject(manifestText);
            return UpdateManifest.from(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean check(@NonNull byte[] publicKeyBuffer,
                                @NonNull byte[] content, @NonNull byte[] signed)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        final String ALGORITHM = "RSA";
        final String SIGN_ALGORITHMS = "SHA1WithRSA";
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBuffer));
        final Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

        signature.initVerify(publicKey);
        signature.update(content);

        return signature.verify(signed);
    }

    private static boolean filterInvalid(@NonNull FileInfo info) {
        return TextUtils.isEmpty(info.version) || TextUtils.isEmpty(info.fileName)
                || TextUtils.isEmpty(info.md5) || TextUtils.isEmpty(info.sha1);
    }

    private static int compareAppVersion(int versionCode) {
        try {
            final Context context = MainApplication.getApplication();
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            final int currentVersion = info.versionCode;

            return versionCode - currentVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static int compareSystemVersion(long versionCode) {
        return (int) (versionCode - (Build.TIME / 1000));
    }

    @Nullable
    private static UpdateManifest filterManifest(@Nullable UpdateManifest manifest) {
        if (manifest == null) {
            return null;
        }

        FileInfo serialInfo = manifest.serial;
        FileInfo appInfo = manifest.app;
        FileInfo systemInfo = manifest.system;

        if (serialInfo != null && filterInvalid(serialInfo)) {
            manifest.serial = null;
        }

        if (appInfo != null && filterInvalid(appInfo)) {
            manifest.app = null;
        }

        if (systemInfo != null && filterInvalid(systemInfo)) {
            manifest.system = null;
        }

        return manifest;
    }

    private int compareManifest(@NonNull UpdateManifest manifest) {
        final int[] results = new int[3];

        if (manifest.serial != null) {
            boolean isBootMode = false;
            try {
                isBootMode = new CheckBootModeCallable().call();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isBootMode) {
                results[0] = 1;
            } else {
                results[0] = checkSerialUpdate(manifest.serial) ? 1 : -1;
            }
        }

        if (manifest.app != null) {
            try {
                final int appVersionCode = Integer.parseInt(manifest.app.version);
                results[1] = compareAppVersion(appVersionCode);
            } catch (Exception e) {
                e.printStackTrace();

                results[1] = -1;
            }
        }

        if (manifest.system != null) {
            try {
                final long systemBuildTime = Long.parseLong(manifest.system.version);
                results[2] = compareSystemVersion(systemBuildTime);
            } catch (Exception e) {
                results[2] = -1;
            }
        }

        if (results[0] <= 0) {
            manifest.serial = null;
        }

        if (results[1] <= 0) {
            manifest.app = null;
        }

        if (results[2] <= 0) {
            manifest.system = null;
        }

        if (results[0] == 0 && results[1] == 0 && results[2] == 0) {
            return 0;
        } else if (results[0] > 0 || results[1] > 0 || results[2] > 0) {
            return 1;
        } else {
            return -1;
        }
    }


    private boolean checkSerialUpdate(FileInfo serial) {
        String serialFile = serial.fileName;
        final File updateFile = mStorage.getUpdateZipFile();
        String updateKey = BuildConfig.UPDATE_PUBLIC_KEY;

        File serialBin = null;

        if (TextUtils.isEmpty(updateKey)) {
            return false;
        }

        final ZipFile zipFile;
        try {
            zipFile = ZipHelper.wrapUpdateZipFile(updateFile);
            if (zipFile == null) {
                return false;
            }

            serialBin = ZipHelper.extract(zipFile, serialFile, updateKey, mStorage.getUpdateCacheDir().getAbsolutePath());
            byte[] serialBytes = FileUtils.bufferlize(serialBin);
            if (serialBytes == null) {
                return false;
            }
            //first 32 byte is version info
            byte[] metaData = new byte[32];
            System.arraycopy(serialBytes, 0, metaData, 0, 32);
            return new CheckUpdateFirmwareCallable(metaData).call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (serialBin != null && serialBin.exists()) {
                serialBin.delete();
            }
        }
    }


    private UpdateManifest getManifest(@NonNull ZipFile zipFile, @NonNull String key) {
        if (ZipHelper.checkFileHeader(zipFile, SIGNED_FILE) && ZipHelper.checkFileHeader(zipFile, MANIFEST_FILE)) {
            final UpdateManifest manifest = filterManifest(exportManifest(zipFile, key, mStorage.getUpdateCacheDir()));

            if (manifest != null) {
                final int compareResult = compareManifest(manifest);

                return compareResult > 0 ? manifest : null;
            }
        }

        return null;
    }

    @Override
    public UpdateManifest call() throws Exception {
        mStorage.resetUpdateCacheDir();
        final File updateFile = mStorage.getUpdateZipFile();

        if (!updateFile.exists()) {
            return null;
        }

        String updateKey = BuildConfig.UPDATE_PUBLIC_KEY;

        if (TextUtils.isEmpty(updateKey)) {
            return null;
        }

        final ZipFile zipFile = ZipHelper.wrapUpdateZipFile(updateFile);

        if (zipFile == null) {
            return null;
        }

        return getManifest(zipFile, updateKey);
    }
}