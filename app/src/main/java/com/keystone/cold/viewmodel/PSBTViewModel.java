package com.keystone.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.BtcImpl;
import com.keystone.coinlib.coins.BTC_NATIVE_SEGWIT.BTC_NATIVE_SEGWIT;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PSBTViewModel extends AndroidViewModel {
    private final String BTCNativeSegwitPath = "M/84'/0'/0'";

    private final BtcImpl btc = new BtcImpl();

    public PSBTViewModel(@NonNull Application application) {
        super(application);
    }

    public PSBT parsePsbtBase64(String psbt) throws InvalidTransactionException {
        PSBT psbt1 = new PSBT();
        try {
            JSONObject object = btc.parsePsbt(psbt);
            JSONArray inputs = object.getJSONArray("inputs");
            JSONArray outputs = object.getJSONArray("outputs");
            psbt1.adoptInputs(inputs);
            psbt1.adoptOutputs(outputs);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidTransactionException("Transaction data error");
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

            public String getMasterFingerprint() {
                return masterFingerprint;
            }

            public String getPath() {
                return path;
            }

            public String getPubkey() {
                return pubkey;
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

            public String getTxId() {
                return txId;
            }

            public int getIndex() {
                return index;
            }

            public long getValue() {
                return value;
            }

            public List<Pubkey> getPubkeys() {
                return pubkeys;
            }

            public boolean isMultiSign() {
                return isMultiSign;
            }

            public String getSignStatus() {
                return signStatus;
            }

            public boolean isFinalized() {
                return isFinalized;
            }

            public String getValueText() {
                return new BigInteger(String.valueOf(getValue())).divide(new BigInteger("10").pow(8)).toString() + " BTC";
            }

            private boolean checkLength() {
                int length = getPubkeys().size();
                // only support single sign currently;
                if (length != 1) return false;
                return true;
            }

            public boolean validate() {
                return checkLength();
            }

            public boolean isMyKey(String myMasterFingerprint) {
                int length = getPubkeys().size();
                for (int i = 0; i < length; i++) {
                    Pubkey pubkey = getPubkeys().get(i);
                    if (pubkey.masterFingerprint.equals(myMasterFingerprint)) {
                        String xpub = new GetExtendedPublicKeyCallable(pubkey.getPath()).call();
                        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
                        String key = Hex.toHexString(extendedPublicKey.getKey());
                        if (pubkey.pubkey.equals(key)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public String getAddress() {
                Pubkey myKey = getPubkeys().get(0);
                String myXpub = new GetExtendedPublicKeyCallable(myKey.getPath()).call();
                return AbsDeriver.newInstance(Coins.BTC_NATIVE_SEGWIT.coinCode()).derive(myXpub);
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

            public String getAddress() {
                return address;
            }

            public long getValue() {
                return value;
            }

            public List<Pubkey> getPubkeys() {
                return pubkeys;
            }

            public String getValueText() {
                return new BigInteger(String.valueOf(getValue())).divide(new BigInteger("10").pow(8)).toString() + " BTC";
            }

            public boolean validate() {
                int length = getPubkeys().size();
                // only support single sign currently;
                return length == 1;
            }

            public boolean isChange(String myMasterFingerprint) {
                int length = getPubkeys().size();
                for (int i = 0; i < length; i++) {
                    Pubkey pubkey = getPubkeys().get(i);
                    if (pubkey.masterFingerprint.equals(myMasterFingerprint)) {
                        String xpub = new GetExtendedPublicKeyCallable(pubkey.getPath()).call();
                        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
                        String key = Hex.toHexString(extendedPublicKey.getKey());
                        if (pubkey.pubkey.equals(key)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public String getChangePath() {
                Pubkey pubkey = getPubkeys().get(0);
                return pubkey.path;
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

        private final List<Input> inputs = new ArrayList<>();
        private final List<Output> outputs = new ArrayList<>();

        public void adoptInputs(JSONArray inputs) throws InvalidTransactionException {
            try {
                int length = inputs.length();
                if (length == 0) {
                    throw new InvalidTransactionException("Transaction has no input");
                }
                for (int i = 0; i < length; i++) {
                    JSONObject jsonInput = inputs.getJSONObject(i);
                    this.inputs.add(Input.fromJSON(jsonInput));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new InvalidTransactionException("Data error");
            }
        }

        public void adoptOutputs(JSONArray outputs) throws InvalidTransactionException {
            try {
                int length = outputs.length();
                if (length == 0) {
                    throw new InvalidTransactionException("Transaction has no output");
                }
                for (int i = 0; i < length; i++) {
                    JSONObject jsonOutput = outputs.getJSONObject(i);
                    this.outputs.add(Output.fromJSON(jsonOutput));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new InvalidTransactionException("Data error");
            }
        }

        private long getFee() {
            long inputValue = this.inputs.stream().map(v -> v.value).reduce(0L, Long::sum);
            long outputValue = this.outputs.stream().map(v -> v.value).reduce(0L, Long::sum);
            return inputValue - outputValue;
        }

        public String getFeeText() {
            return new BigInteger(String.valueOf(getFee())).divide(new BigInteger("10").pow(8)).toString() + " BTC";
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public List<Output> getOutputs() {
            return outputs;
        }

        public void validate(String masterFingerprint) throws InvalidTransactionException {
            this.validateInputs(masterFingerprint);
            this.validateOutputs();
        }

        private void validateInputs(String masterFingerprint) throws InvalidTransactionException {
            for (int i = 0; i < this.inputs.size(); i++) {
                Input input = this.inputs.get(i);
                if (input.validate() && input.isMyKey(masterFingerprint)) return;
            }
            throw new InvalidTransactionException("Transaction has no valid input");
        }

        private void validateOutputs() throws InvalidTransactionException {
            for (int i = 0; i < this.outputs.size(); i++) {
                Output output = this.outputs.get(i);
                if (output.validate()) return;
            }
            throw new InvalidTransactionException("Transaction has no valid output");
        }
    }
}


