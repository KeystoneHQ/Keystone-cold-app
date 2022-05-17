package com.keystone.coinlib.selector;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.keystone.coinlib.utils.SDCardUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TFCardSelectorStore implements SelectorStoreEngine{

    private static final String DATABASE_TFCARD_PATH = "selector" + File.separator + "ethereum";


    @Override
    public List<MethodSignature> load(String signature) {
        List<MethodSignature>  methodSignatures = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase;
        try {
            String databaseFilePath = SDCardUtil.externalSDCardPath() + File.separator
                    + DATABASE_TFCARD_PATH + File.separator + "method_signatures.db";
            sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            e.printStackTrace();
            return methodSignatures;
        }

        try (Cursor cursor = sqLiteDatabase.query("SELECTOR", null, "METHOD_SIGNATURE='" + signature + "'",
                null, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    MethodSignature methodSignature  = new MethodSignature();
                    methodSignature.setSignature(cursor.getString(cursor.getColumnIndex("METHOD_SIGNATURE")));
                    methodSignature.setMethodName(cursor.getString(cursor.getColumnIndex("METHOD_NAME")));
                    methodSignatures.add(methodSignature);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.close();
        }
        return methodSignatures;
    }
}
