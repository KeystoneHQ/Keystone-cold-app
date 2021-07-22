package com.keystone.coinlib.abi;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.keystone.coinlib.utils.SDCardUtil;

import java.io.File;

public class TFCardABIStore implements ABIStoreEngine {
    private static final String DATABASE_TFCARD_PATH = "contracts" + File.separator + "ethereum";
    private String address;

    public TFCardABIStore(String address) {
        this.address = address;
    }

    @Override
    public Contract load() {
        SQLiteDatabase sqLiteDatabase;
        Contract contract = new Contract();
        try {
            String databaseFilePath = SDCardUtil.externalSDCardPath() + File.separator
                    + DATABASE_TFCARD_PATH + File.separator + "contracts.db";
            sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
            return contract;
        }
        try (Cursor cursor = sqLiteDatabase.query("contracts", null, "address='" + address + "'",
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                contract.setName(cursor.getString(cursor.getColumnIndex("name")));
                contract.setMetadata(cursor.getString(cursor.getColumnIndex("metadata")));
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
