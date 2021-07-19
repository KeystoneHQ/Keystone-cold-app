package com.keystone.coinlib.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import com.keystone.coinlib.Coinlib;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class AbiLoader {
    public static final String INDEX_JSON_SDCARD_PATH = "contracts" + File.separator + "ethereum";

    public static String getNameFromTFCard(String address) {
        SQLiteDatabase db = getDb();
        if (TextUtils.isEmpty(address) || db == null) {
            return null;
        }
        try (Cursor cursor = db.query("contracts", new String[]{"name"}, "address='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                return cursor.getString(cursor.getColumnIndex("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return null;
    }

    public static String getAbiFromTFCard(String address) {
        SQLiteDatabase db = getDb();
        if (TextUtils.isEmpty(address) || db == null) {
            return null;
        }
        try (Cursor cursor = db.query("contracts", new String[]{"metadata"}, "address='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                return cursor.getString(cursor.getColumnIndex("metadata"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return null;
    }

    private static SQLiteDatabase getDb() {
        try {
            String databaseFilePath = externalSDCardPath() + File.separator + INDEX_JSON_SDCARD_PATH + File.separator + "contracts";
            return SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
}
