package com.keystone.coinlib.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.keystone.coinlib.Coinlib;

import java.lang.reflect.Method;
import java.util.List;

public class SDCardUtil {
    public static String externalSDCardPath() {
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
}
