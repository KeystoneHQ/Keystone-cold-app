package com.keystone.coinlib.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;

import com.keystone.coinlib.Coinlib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class AbiLoader {
    private static final String TAG = "AbiLoader";

    public static final String INDEX_JSON_SDCARD_PATH = "contracts";

    public static String getContentFromSdCard(String address) {
        if (TextUtils.isEmpty(externalSDCardPath())) {
            Log.i(TAG, "sdCard is not exists");
            return "";
        }
        String content = null;
        try {
            String indexPath = externalSDCardPath() + File.separator + INDEX_JSON_SDCARD_PATH + File.separator + "index.json";
            String indexString = readFromFile(indexPath);
            JSONObject indexJson = new JSONObject(indexString);
            String filename = indexJson.getString(address);
            String contractsString = readFromFile(externalSDCardPath() + File.separator + INDEX_JSON_SDCARD_PATH + File.separator + filename);
            JSONObject contractsJson = new JSONObject(contractsString);
            content = contractsJson.getString(address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }

    private static String externalSDCardPath() {
        String sdCardPath = "";
        try {
            StorageManager storageManager = (StorageManager) Coinlib.sInstance.getContext().getSystemService(Context.STORAGE_SERVICE);

            // Android N started to have this method
            List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
            Class<?> volumeClass = Class.forName("android.os.storage.StorageVolume");
            Method getPath = volumeClass.getDeclaredMethod("getPath");
            getPath.setAccessible(true);
            StorageVolume storageVolume = storageVolumes.get(storageVolumes.size() - 1);
            sdCardPath = (String) getPath.invoke(storageVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdCardPath;
    }

    public static String readFromFile(String filepath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bfr = new BufferedReader(new FileReader(filepath))) {
            String line = bfr.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bfr.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
