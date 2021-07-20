package com.keystone.coinlib.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import com.keystone.coinlib.Coinlib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class ContractExternalDbLoader {
    public static final String DATABASE_TFCARD_PATH = "contracts" + File.separator + "ethereum";

    public static Contract contractData(String address) {
        SQLiteDatabase db = getDb();
        Contract contract = new Contract();
        if (TextUtils.isEmpty(address) || db == null) {
            return contract;
        }
        try (Cursor cursor = db.query("contracts", null, "address='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                contract.setName(cursor.getString(cursor.getColumnIndex("name")));
                contract.setMetadata(cursor.getString(cursor.getColumnIndex("metadata")));
                return contract;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return contract;
    }

    private static SQLiteDatabase getDb() {
        try {
            String databaseFilePath = externalSDCardPath() + File.separator + DATABASE_TFCARD_PATH + File.separator + "contracts.db";
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

    public static class Contract {
        private String name;

        private String metadata;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getAbi() {
            String abi = null;
            try {
                JSONObject metaData = new JSONObject(metadata);
                JSONObject output = metaData.getJSONObject("output");
                abi = output.getString("abi");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return abi;
        }
    }
}
