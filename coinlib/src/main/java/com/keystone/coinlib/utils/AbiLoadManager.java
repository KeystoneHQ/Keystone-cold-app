package com.keystone.coinlib.utils;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class AbiLoadManager {
    private static final String DATABASE_TFCARD_PATH = "contracts" + File.separator + "ethereum";
    private static final String SELF_DEFINE_TFCARD_PATH = "contracts" + File.separator + "self_define";
    private String address;
    private boolean isFromTFCard;
    private Contract contract;
    private JSONObject bundleMap;

    public AbiLoadManager(String address) {
        this.address = address.toLowerCase();
        contract = new Contract();
        try {
            bundleMap = new JSONObject(readAsset("abi/abiMap.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Contract loadAbi() {
        if (TextUtils.isEmpty(address)) {
            return contract;
        }
        loadFromAssets();
        loadFromDB();
        loadFromSelfDefine();
        return contract;
    }

    private void loadFromAssets() {
        String abiFile = bundleMap.optString(address);
        if (!TextUtils.isEmpty(abiFile)) {
            contract.setAbi(readAsset("abi/" + abiFile));
            contract.setName(abiFile.replace(".json", ""));
        }
    }

    private void loadFromDB() {
        if (contract.isEmpty()) {
            SQLiteDatabase sqLiteDatabase;
            try {
                String databaseFilePath = FileUtil.externalSDCardPath() + File.separator
                        + DATABASE_TFCARD_PATH + File.separator + "contracts.db";
                sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFilePath, null, SQLiteDatabase.OPEN_READONLY);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
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
            if (contract.isEmpty()) {
                isFromTFCard = true;
            }
        }
    }

    private void loadFromSelfDefine() {
        if (contract.isEmpty()) {
            String selfDefineFilePath = FileUtil.externalSDCardPath() + File.separator
                    + SELF_DEFINE_TFCARD_PATH + File.separator + address + ".json";
            String content = FileUtil.readFromFile(selfDefineFilePath);
            try {
                JSONObject contentJson = new JSONObject(content);
                JSONObject metadataJson = contentJson.getJSONObject("metadata");
                JSONObject outputJson = metadataJson.getJSONObject("output");
                contract.setAbi(outputJson.getString("abi"));
                contract.setName(contentJson.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (contract.isEmpty()) {
                isFromTFCard = true;
            }
        }
    }

    public boolean isFromTFCard() {
        return isFromTFCard;
    }

    public static class Contract {
        private String name;

        private String abi;

        private String metadata;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAbi() {
            return abi;
        }

        public void setAbi(String abi) {
            this.abi = abi;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
            this.abi = createAbi();
        }

        private String createAbi() {
            String abi = null;
            if (metadata == null) {
                return abi;
            }
            try {
                JSONObject metaData = new JSONObject(metadata);
                JSONObject output = metaData.getJSONObject("output");
                abi = output.getString("abi");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return abi;
        }

        public boolean isEmpty() {
            return abi == null;
        }
    }
}
