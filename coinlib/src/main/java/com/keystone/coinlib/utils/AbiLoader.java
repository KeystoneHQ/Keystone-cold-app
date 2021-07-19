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
    private static SQLiteDatabase db;

    public static String getNameFromTFCard(String address) {
        if (TextUtils.isEmpty(address) || getDb() == null) {
            return null;
        }
        try (Cursor cursor = db.query("contracts", new String[]{"name"}, "address='" + address + "'",
                null, null, null, null)) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("name"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAbiFromTFCard(String address) {
        if (TextUtils.isEmpty(address) || getDb() == null) {
            return null;
        }
        try (Cursor cursor = db.query("ethabis", new String[]{"metadata"}, "address='" + address + "'",
                null, null, null, null)) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("metadata"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SQLiteDatabase getDb() {
        if (db != null) {
            return db;
        }
        try {
            String databaseFilename = externalSDCardPath() + File.separator + INDEX_JSON_SDCARD_PATH + File.separator + "contracts.db";
            File file = new File(databaseFilename);
            if (file.exists()) {
                db = SQLiteDatabase.openDatabase(databaseFilename, null, SQLiteDatabase.OPEN_READONLY);
            }
            return db;
        } catch (Exception e) {
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
