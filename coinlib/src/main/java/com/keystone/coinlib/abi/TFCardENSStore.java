package com.keystone.coinlib.abi;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.keystone.coinlib.utils.SDCardUtil;

import java.io.File;

public class TFCardENSStore implements ABIStoreEngine {
    private static final String DATABASE_TFCARD_PATH = "ens";

    @Override
    public Contract load(String address) {
        SQLiteDatabase sqLiteDatabase;
        Contract contract = new Contract();
        try {
            String databaseFilePath = SDCardUtil.externalSDCardPath() + File.separator
                    + DATABASE_TFCARD_PATH + File.separator + "ENS.db";
            sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
            return contract;
        }
        try (Cursor cursor = sqLiteDatabase.query("ens", null, "addr='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                contract.setName(cursor.getString(cursor.getColumnIndex("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.close();
        }
        contract.setFromTFCard(true);
        return contract;
    }
}
