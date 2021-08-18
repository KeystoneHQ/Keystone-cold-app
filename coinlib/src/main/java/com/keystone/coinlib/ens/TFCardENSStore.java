package com.keystone.coinlib.ens;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.keystone.coinlib.utils.SDCardUtil;

import java.io.File;

public class TFCardENSStore implements ENSStoreEngine {
    private static final String DATABASE_TFCARD_PATH = "ens";

    @Override
    public String load(String address) {
        SQLiteDatabase sqLiteDatabase;
        try {
            String databaseFilePath = SDCardUtil.externalSDCardPath() + File.separator
                    + DATABASE_TFCARD_PATH + File.separator + "ENS.db";
            sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        try (Cursor cursor = sqLiteDatabase.query("ens", null, "addr='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                return cursor.getString(cursor.getColumnIndex("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.close();
        }
        return null;
    }
}
