package com.keystone.cold.viewmodel.tx.psbt;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.coins.BTC_LEGACY.BTC_LEGACY;
import com.keystone.coinlib.coins.BTC_NATIVE_SEGWIT.BTC_NATIVE_SEGWIT;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PSBT {
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

        // derived props;
        private boolean isMine;
        private String canonicalHDPath;
        private String canonicalPubkey;
        private String canonicalAddress;

        public String getCanonicalAddress() {
            return canonicalAddress;
        }

        public Input(String txId, int index, long value, List<Pubkey> pubkeys, boolean isMultiSign, String signStatus, boolean isFinalized, boolean isMine, String canonicalHDPath, String canonicalPubkey, String canonicalAddress) {
            this.txId = txId;
            this.index = index;
            this.value = value;
            this.pubkeys = pubkeys;
            this.isMultiSign = isMultiSign;
            this.signStatus = signStatus;
            this.isFinalized = isFinalized;
            this.isMine = isMine;
            this.canonicalHDPath = canonicalHDPath;
            this.canonicalPubkey = canonicalPubkey;
            this.canonicalAddress = canonicalAddress;
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
            return new BigDecimal(String.valueOf(getValue())).divide(new BigDecimal("10").pow(8)).toString() + " BTC";
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

        public String getAddress() {
            Pubkey myKey = getPubkeys().get(0);
            String myXpub = new GetExtendedPublicKeyCallable(myKey.getPath()).call();
            return AbsDeriver.newInstance(Coins.BTC_NATIVE_SEGWIT.coinCode()).derive(myXpub);
        }

        public boolean isMine() {
            return isMine;
        }

        public String getCanonicalHDPath() {
            return canonicalHDPath;
        }

        public String getCanonicalPubkey() {
            return canonicalPubkey;
        }

        private static String deriveAddress(String path, String pubkey) {
            // do not support multisig yet;
            if (path.startsWith(PSBTViewModel.BTCNestedSegwitPath)) {
                return new Btc.Deriver().deriveByPubkey(pubkey);
            } else if (path.startsWith(PSBTViewModel.BTCLegacyPath)) {
                return new BTC_LEGACY.Deriver().deriveByPubkey(pubkey);
            } else {
                return new BTC_NATIVE_SEGWIT.Deriver().deriveByPubkey(pubkey);
            }
        }

        @Override
        public String toString() {
            return "Input{" +
                    "txId='" + txId + '\'' +
                    ", index=" + index +
                    ", value=" + value +
                    ", pubkeys=" + pubkeys +
                    ", isMultiSign=" + isMultiSign +
                    ", signStatus='" + signStatus + '\'' +
                    ", isFinalized=" + isFinalized +
                    ", isMine=" + isMine +
                    ", canonicalHDPath='" + canonicalHDPath + '\'' +
                    ", canonicalPubkey='" + canonicalPubkey + '\'' +
                    ", canonicalAddress='" + canonicalAddress + '\'' +
                    '}';
        }

        public static Input fromJSON(JSONObject json, String myMasterFingerprint) throws JSONException {
            String txId = json.getString("txId");
            int index = json.getInt("index");
            long value = json.getLong("value");
            JSONArray keys = json.getJSONArray("hdPath");
            List<Pubkey> pubkeys = new ArrayList<>();
            int length = keys.length();
            String canonicalHDPath = null;
            String canonicalPubkey = null;
            String canonicalAddress = null;
            for (int i = 0; i < length; i++) {
                JSONObject key = keys.getJSONObject(i);
                String masterFingerprint = key.getString("masterFingerprint");
                String path = key.getString("path");
                String pubkey = key.getString("pubkey");
                Pubkey publicKey = new Pubkey(masterFingerprint, path, pubkey);
                canonicalPubkey = pubkey;
                if (masterFingerprint.equalsIgnoreCase(myMasterFingerprint)) {
                    canonicalHDPath = calculateCanonicalPath(publicKey);
                    if (canonicalHDPath != null) {
                        canonicalAddress = deriveAddress(canonicalHDPath, canonicalPubkey);
                    }
                }
                pubkeys.add(publicKey);
            }
            boolean isMultiSign = json.getBoolean("isMultiSign");
            String signStatus = json.optString("signStatus");
            boolean isFinalized = json.getBoolean("isFinalized");
            return new Input(txId, index, value, pubkeys, isMultiSign, signStatus, isFinalized, canonicalHDPath != null, canonicalHDPath, canonicalPubkey, canonicalAddress);
        }
    }

    public static class Output {
        @Override
        public String toString() {
            return "Output{" +
                    "address='" + address + '\'' +
                    ", value=" + value +
                    ", pubkeys=" + pubkeys +
                    ", isChange=" + isChange +
                    ", changePath='" + changePath + '\'' +
                    '}';
        }

        private String address;
        private long value;
        private List<Pubkey> pubkeys;

        private boolean isChange;
        private String changePath;

        public Output(String address, long value, List<Pubkey> pubkeys, boolean isChange, String changePath) {
            this.address = address;
            this.value = value;
            this.pubkeys = pubkeys;
            this.isChange = isChange;
            this.changePath = changePath;
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
            return new BigDecimal(String.valueOf(getValue())).divide(new BigDecimal("10").pow(8)).toString() + " BTC";
        }

        public String getChangePath() {
            Pubkey pubkey = getPubkeys().get(0);
            return pubkey.path;
        }

        public static Output fromJSON(JSONObject json, String myMasterFingerprint) throws JSONException {
            String address = json.getString("address");
            long value = json.getLong("value");
            JSONArray keys = json.optJSONArray("hdPath");
            String canonicalHDPath = null;
            List<Pubkey> pubkeys = new ArrayList<>();
            if (keys != null) {
                int length = keys.length();
                for (int i = 0; i < length; i++) {
                    JSONObject key = keys.getJSONObject(i);
                    String masterFingerprint = key.getString("masterFingerprint");
                    String path = key.getString("path");
                    String pubkey = key.getString("pubkey");
                    Pubkey publicKey = new Pubkey(masterFingerprint, path, pubkey);
                    if (masterFingerprint.equalsIgnoreCase(myMasterFingerprint)) {
                        canonicalHDPath = calculateCanonicalPath(publicKey);
                    }
                    pubkeys.add(publicKey);
                }
            }
            return new Output(address, value, pubkeys, canonicalHDPath != null, canonicalHDPath);
        }
    }

    private final List<Input> inputs = new ArrayList<>();
    private final List<Output> outputs = new ArrayList<>();
    private final String myMasterFingerprint;
    private final String rawData;

    public PSBT(String rawData, String myMasterFingerprint) {
        this.rawData = rawData;
        this.myMasterFingerprint = myMasterFingerprint;
    }

    public String getRawData() {
        return rawData;
    }

    public void adoptInputs(JSONArray inputs) throws InvalidTransactionException {
        try {
            int length = inputs.length();
            if (length == 0) {
                throw new InvalidTransactionException("Transaction has no input");
            }
            for (int i = 0; i < length; i++) {
                JSONObject jsonInput = inputs.getJSONObject(i);
                this.inputs.add(Input.fromJSON(jsonInput, myMasterFingerprint));
            }
            if (this.getMySigningInputs().size() == 0) {
                throw new InvalidTransactionException("Transaction is not related to this Wallet");
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
                this.outputs.add(Output.fromJSON(jsonOutput, myMasterFingerprint));
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
        return new BigDecimal(String.valueOf(getFee())).divide(new BigDecimal("10").pow(8)).toString() + " BTC";
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public List<Input> getMySigningInputs() {
        return this.inputs.stream().filter(v -> v.isMine).collect(Collectors.toList());
    }

    private static String calculateCanonicalPath(Pubkey pubkey) {
        AtomicReference<String> canonicalHDPath = new AtomicReference<>(null);
        PSBTViewModel.BTCPaths.forEach(a -> {
            String path = pubkey.path;
            String xpub = new GetExtendedPublicKeyCallable(path).call();
            ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
            if (Hex.toHexString(extendedPublicKey.getKey()).equalsIgnoreCase(pubkey.pubkey)) {
                canonicalHDPath.set(path);
            }
        });
        return canonicalHDPath.get();
    }

    public JSONObject generateParsedMessage() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray inputs = new JSONArray();
        int length = this.getInputs().size();
        for (int i = 0; i < length; i++) {
            Input target = this.getInputs().get(i);
            JSONObject input = new JSONObject();
            input.put("is_mine", target.isMine);
            input.put("address", target.canonicalAddress);
            input.put("path", target.canonicalHDPath);
            input.put("value", target.getValueText());
            input.put("pubkey", target.canonicalPubkey);
            inputs.put(input);
        }

        JSONArray outputs = new JSONArray();

        length = this.getOutputs().size();
        for (int i = 0; i < length; i++) {
            Output target = this.getOutputs().get(i);
            JSONObject output = new JSONObject();
            output.put("is_change", target.isChange);
            output.put("change_path", target.changePath);
            output.put("address", target.address);
            output.put("value", target.getValueText());
            outputs.put(output);
        }

        object.put("_version", 0);
        object.put("inputs", inputs);
        object.put("outputs", outputs);
        object.put("fee", this.getFeeText());

        return object;
    }
}
