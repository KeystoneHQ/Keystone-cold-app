package com.keystone.coinlib.abi;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.keystone.coinlib.utils.SDCardUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TFCardABIStore implements ABIStoreEngine {
    private static final String DATABASE_TFCARD_PATH = "contracts" + File.separator + "ethereum";

    @Override
    public List<Contract> load(String address) {
        List<Contract>  contracts = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase;
        try {
            String databaseFilePath = SDCardUtil.externalSDCardPath() + File.separator
                    + DATABASE_TFCARD_PATH + File.separator + "contracts.db";
            sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
            return contracts;
        }
        try (Cursor cursor = sqLiteDatabase.query("contracts", null, "address='" + address + "'",
                null, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Contract contract = new Contract();
                    contract.setFromTFCard(true);
                    contract.setName(cursor.getString(cursor.getColumnIndex("name")));
                    contract.setMetadata(cursor.getString(cursor.getColumnIndex("metadata")));
                    contracts.add(contract);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.close();
        }
        return contracts;
    }
}
