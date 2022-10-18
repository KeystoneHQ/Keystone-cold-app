package com.keystone.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.keystone.coinlib.coins.BTC.BtcImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PSBTViewModel extends AndroidViewModel {
    private final BtcImpl btc = new BtcImpl();

    public PSBTViewModel(@NonNull Application application) {
        super(application);
    }

    public PSBT parsePsbtBase64(String psbt) {
        PSBT psbt1 = new PSBT();
        try {
            JSONObject object = btc.parsePsbt(psbt);
            JSONArray inputs = object.getJSONArray("inputs");
            JSONArray outputs = object.getJSONArray("outputs");
            psbt1.adoptInputs(inputs);
            psbt1.adoptOutputs(outputs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return psbt1;
    }

    public static class PSBT {
        public static class Pubkey {
            private String masterFingerprint;
            private String path;
            private String pubkey;

            public Pubkey(String masterFingerprint, String path, String pubkey) {
                this.masterFingerprint = masterFingerprint;
                this.path = path;
                this.pubkey = pubkey;
            }
        }

        public static class Input {
            private String txId;
            private int index;
            private long value;
            private List<Pubkey> pubkeys;
            private boolean isMultiSign;
            private String signStatus;
            private boolean isFinalized;

            public Input(String txId, int index, long value, List<Pubkey> pubkeys, boolean isMultiSign, String signStatus, boolean isFinalized) {
                this.txId = txId;
                this.index = index;
                this.value = value;
                this.pubkeys = pubkeys;
                this.isMultiSign = isMultiSign;
                this.signStatus = signStatus;
                this.isFinalized = isFinalized;
            }

            public static Input fromJSON(JSONObject json) throws JSONException {
                String txId = json.getString("txId");
                int index = json.getInt("index");
                long value = json.getLong("value");
                JSONArray keys = json.getJSONArray("hdPath");
                List<Pubkey> pubkeys = new ArrayList<>();
                int length = keys.length();
                for (int i = 0; i < length; i++) {
                    JSONObject key = keys.getJSONObject(i);
                    String masterFingerprint = key.getString("masterFingerprint");
                    String path = key.getString("path");
                    String pubkey = key.getString("pubkey");
                    pubkeys.add(new Pubkey(masterFingerprint, path, pubkey));
                }
                boolean isMultiSign = json.getBoolean("isMultiSign");
                String signStatus = json.getString("signStatus");
                boolean isFinalized = json.getBoolean("isFinalized");
                return new Input(txId, index, value, pubkeys, isMultiSign, signStatus, isFinalized);
            }
        }

        public static class Output {
            private String address;
            private long value;
            private List<Pubkey> pubkeys;

            public Output(String address, long value, List<Pubkey> pubkeys) {
                this.address = address;
                this.value = value;
                this.pubkeys = pubkeys;
            }

            public static Output fromJSON(JSONObject json) throws JSONException {
                String address = json.getString("address");
                long value = json.getLong("value");
                JSONArray keys = json.getJSONArray("hdPath");
                List<Pubkey> pubkeys = new ArrayList<>();
                int length = keys.length();
                for (int i = 0; i < length; i++) {
                    JSONObject key = keys.getJSONObject(i);
                    String masterFingerprint = key.getString("masterFingerprint");
                    String path = key.getString("path");
                    String pubkey = key.getString("pubkey");
                    pubkeys.add(new Pubkey(masterFingerprint, path, pubkey));
                }

                return new Output(address, value, pubkeys);
            }
        }

        private List<Input> inputs = new ArrayList<>();
        private List<Output> outputs = new ArrayList<>();

        private boolean isError = false;

        public void adoptInputs(JSONArray inputs) {
            try {
                int length = inputs.length();
                if (length == 0) {
                    this.isError = true;
                    return;
                }
                for (int i = 0; i < length; i++) {
                    JSONObject jsonInput = inputs.getJSONObject(i);
                    this.inputs.add(Input.fromJSON(jsonInput));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.isError = true;
            }
        }

        public void adoptOutputs(JSONArray outputs) {
            try {
                int length = outputs.length();
                if (length == 0) {
                    this.isError = true;
                    return;
                }
                for (int i = 0; i < length; i++) {
                    JSONObject jsonOutput = outputs.getJSONObject(i);
                    this.outputs.add(Output.fromJSON(jsonOutput));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.isError = true;
            }
        }

        public boolean isError() {
            return this.isError;
        }
    }
}


