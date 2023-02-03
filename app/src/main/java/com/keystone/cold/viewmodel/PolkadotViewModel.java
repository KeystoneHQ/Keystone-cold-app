package com.keystone.cold.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.SDCardUtil;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.cryptocore.PolkadotService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolkadotViewModel extends AndroidViewModel {
    private final String dbPath;
    @SuppressLint("StaticFieldLeak")
    private final Context context;

    public PolkadotViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        dbPath = context.getFilesDir().getPath() + "/polkadot/database";
    }

    private void copySingleAsset(String path) throws IOException {
        File file = new File(dbPath, path);
        file.createNewFile();
        InputStream input = context.getAssets().open(Paths.get("polkadot", "database", path).toString());
        FileOutputStream out = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int read = input.read(b);
        while (read != -1) {
            out.write(b, 0, read);
            read = input.read(b);
        }
        out.close();
        input.close();
    }

    private void recursivelyRemove(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    recursivelyRemove(file1);
                }
            }
        }
        file.delete();
    }

    private void cleanDB() {
        File file = new File(context.getFilesDir().getPath() + "/polkadot");
        recursivelyRemove(file);
    }

    private boolean copyAssets(String path) {
        try {
            String[] contents = context.getAssets().list("polkadot/database" + path);
            if (contents == null || contents.length == 0) {
                copySingleAsset(path);
            } else {
                new File(dbPath, path).mkdirs();
                for (String e :
                        contents) {

                    copyAssets(path + "/" + e);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initialDB() throws PolkadotException {
        if (Utilities.getPolkadotDBInitialized(context)) {
            return;
        }
        if (!this.resetDB()) {
            throw new PolkadotException("Initialize Polkadot DB failed");
        }
    }

    public boolean resetDB() {
        cleanDB();
        Utilities.setPolkadotDbInitialized(context, false);
        if (!this.copyAssets("")) {
            return false;
        }
        String response = PolkadotService.initialDB(dbPath);
        try {
            JSONObject json = new JSONObject(response);
            if (json.getString("status").equals("success")) {
                Utilities.setPolkadotDbInitialized(context, true);
            } else {
                String reason = json.getString("reason");
                throw new PolkadotException(reason);
            }
        } catch (JSONException | PolkadotException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public JSONObject parseTransaction(String transactionHex) throws PolkadotException {
        String response = PolkadotService.parse(transactionHex, dbPath);
        try {
            JSONObject json = new JSONObject(response);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new PolkadotException("Invalid Response");
        }
    }

    public MutableLiveData<JSONObject> parseTransactionAsync(String transactionHex){
        MutableLiveData<JSONObject> result = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            String response = PolkadotService.parse(transactionHex, dbPath);
            try {
                result.postValue(new JSONObject(response));
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    result.postValue(new JSONObject().put("status", "failed"));
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        });
        return result;
    }

    public JSONObject handleStub(int checksum) throws PolkadotException {
        String response = PolkadotService.handleStub(dbPath, checksum);
        try {
            JSONObject json = new JSONObject(response);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new PolkadotException("Invalid Response");
        }
    }

    public JSONObject getSignContent(int checksum) throws PolkadotException {
        String response = PolkadotService.getSignContent(dbPath, checksum);
        try {
            JSONObject json = new JSONObject(response);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new PolkadotException("Invalid Response");
        }
    }

    public void importAddress(String public_key, String path) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String response = PolkadotService.importAddress(dbPath, public_key, path);
            try {
                JSONObject json = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public static class PolkadotException extends Exception {
        PolkadotException(String reason) {
            super(reason);
        }
    }

    public static class PolkadotDecoder {
        private int total;
        private List<String> messages;

        public List<String> getMessages() {
            return this.messages;
        }

        public PolkadotDecoder() {
            total = 0;
            messages = new ArrayList<>();
        }

        public int getTotal() {
            return total;
        }

        public int getCurrent() {
            return messages.size();
        }

        public double getPercentage() {
            return ((double) getCurrent()) / (getTotal());
        }

        public void reset() {
            total = 0;
            messages = new ArrayList<>();
        }

        private int getPacketsTotal(String scanned) throws PolkadotException {
            String response = PolkadotService.getPacketsTotal(scanned);
            try {
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equals("success")) {
                    return json.getInt("value");
                } else {
                    String reason = json.getString("reason");
                    throw new PolkadotException(reason);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new PolkadotException("Invalid Response");
            }
        }

        public String decode() throws PolkadotException {
            if (this.getCurrent() < this.getTotal()) return null;
            String response = PolkadotService.decodeSequence(this.messages);
            try {
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equals("success")) {
                    return json.getString("value");
                } else {
                    // try 5 times again for multi frames
                    if (this.getCurrent() < this.getTotal() + 5) return null;
                    String reason = json.getString("reason");
                    throw new PolkadotException(reason);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new PolkadotException("Invalid Response");
            }
        }

        public boolean tryReadFirst(String message) {
            try {
                total = this.getPacketsTotal(message);
                this.addMessage(message);
                return true;
            } catch (PolkadotException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void addMessage(String message) {
            if (!messages.contains(message)) {
                messages.add(message);
            }
        }

        public boolean receive(String message) throws PolkadotException {
            int newTotal = this.getPacketsTotal(message);
            if (total != newTotal) {
                return false;
            }
            this.addMessage(message);
            return true;
        }
    }
}