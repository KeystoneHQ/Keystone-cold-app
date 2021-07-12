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

package com.keystone.coinlib.v8;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;

import com.eclipsesource.v8.V8;
import com.keystone.coinlib.Coinlib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

public class ScriptLoader {
    private static final String TAG = "ScriptLoader";

    public static ScriptLoader sInstance;

    private ScriptLoader() {
    }

    public static void init() {
        if (sInstance == null) {
            synchronized (ScriptLoader.class) {
                if (sInstance == null) {
                    sInstance = new ScriptLoader();
                }
            }
        }
    }

    public V8 loadByCoinCode(String coinCode) {
        V8 v8 = V8.createV8Runtime("window");
        String js = getJs(coinCode);
        if (!TextUtils.isEmpty(js) && !v8.isReleased()) {
            v8.executeVoidScript(js);
        }
        return v8;
    }

    private String getJs(String coinCode) {
        try {
            JSONObject bundleMap = new JSONObject(readAsset("bundleMap.json"));
            return readAsset("script/" + bundleMap.getString(coinCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readAsset(String fileName) {
        AssetManager am = Coinlib.sInstance.getContext().getAssets();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = am.open(fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line).append("\r\n");
            }
            bf.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String getContentFromSdCard(String path, String fileName) {
        if (TextUtils.isEmpty(externalSDCardPath())) {
            Log.d(TAG, "sdCard is not exists");
            return "";
        }
        File file = new File(externalSDCardPath() + File.separator + path, fileName + ".json");
        if (!file.exists()) {
            Log.d(TAG, file.getAbsolutePath() + " is not exists");
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Log.d(TAG, file.getAbsolutePath() + " is exists");
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new FileReader(file));
            String line = bfr.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bfr.readLine();
            }
            Log.d(TAG, "bufferRead: " + stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bfr != null) {
                try {
                    bfr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    private static String externalSDCardPath() {
        String sdCardPath = "";
        try {
            StorageManager storageManager = (StorageManager) Coinlib.sInstance.getContext().getSystemService(Context.STORAGE_SERVICE);
            // 7.0才有的方法
            List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
            Class<?> volumeClass = Class.forName("android.os.storage.StorageVolume");
            Method getPath = volumeClass.getDeclaredMethod("getPath");
            Method isRemovable = volumeClass.getDeclaredMethod("isRemovable");
            getPath.setAccessible(true);
            isRemovable.setAccessible(true);
            StorageVolume storageVolume = storageVolumes.get(storageVolumes.size() - 1);
            sdCardPath = (String) getPath.invoke(storageVolume);
            Boolean isRemove = (Boolean) isRemovable.invoke(storageVolume);
            if (storageVolumes.size() > 1) {
                Log.d(TAG, "externalSDCardPath is === " + sdCardPath);
                Log.d(TAG, "isRemoveble == " + isRemove);
            } else {
                Log.d(TAG, "Built-in sd card path is === " + sdCardPath);
                Log.d(TAG, "isRemoveble == " + isRemove);
                Log.d(TAG, "no sd card inserted");
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdCardPath;
    }

}
