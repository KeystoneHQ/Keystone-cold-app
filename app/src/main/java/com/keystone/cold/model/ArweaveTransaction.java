package com.keystone.cold.model;

import com.keystone.coinlib.Util;
import com.keystone.cold.integration.chains.ArweaveViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ArweaveTransaction {
    public static final String AR_KEY_OWNER = "owner";
    public static final String AR_KEY_TARGET = "target";
    public static final String AR_KEY_QUANTITY = "quantity";
    public static final String AR_KEY_ID = "id";
    public static final String AR_KEY_REWARD = "reward";
    public static final String AR_KEY_TAGS = "tags";
    public static final String AR_KEY_SIGNATURE = "signature";
    public static final String AR_KEY_DATA = "data";
    public static final String AR_KEY_SIGNATURE_DATA = "signature_data";
    public static final String AR_KEY_FORMATTED_JSON = "formatted_json";
    public static final String AR_KEY_RAW_JSON = "raw_json";

    private final String from;
    private final String to;
    private final String value;
    private final String fee;
    private final String id;
    private final String data;
    private final Tag[] tags;
    private final String signature;
    private String signatureData;

    private final JSONObject rawTx;

    private String signatureUR;

    public static ArweaveTransaction fromJSON(JSONObject rawTx) throws JSONException {
        String owner = rawTx.getString(AR_KEY_OWNER);
        String from = ArweaveViewModel.formatHex(Util.sha256(Base64.getUrlDecoder().decode(owner)));
        String to = rawTx.getString(AR_KEY_TARGET);
        BigDecimal divider = new BigDecimal(String.valueOf(10)).pow(12);
        String value = rawTx.optString(AR_KEY_QUANTITY);

        DecimalFormat df = new DecimalFormat();

        df.setMaximumFractionDigits(12);

        df.setMinimumFractionDigits(0);

        df.setGroupingUsed(false);

        value = df.format(new BigDecimal(value).setScale(12, RoundingMode.HALF_UP).divide(divider, RoundingMode.HALF_UP)) + " AR";
        String fee = rawTx.optString(AR_KEY_REWARD);
        fee = df.format(new BigDecimal(fee).setScale(12, RoundingMode.HALF_UP).divide(divider, RoundingMode.HALF_UP)) + " AR";
        String id = rawTx.optString(AR_KEY_ID);

        if (rawTx.optJSONArray(AR_KEY_TAGS) != null) {
            Tag[] tags = Tag.fromJSONArray(rawTx.getJSONArray(AR_KEY_TAGS));
            String signature = rawTx.optString(AR_KEY_SIGNATURE);
            String data = rawTx.optString(AR_KEY_DATA);
            return new ArweaveTransaction(from, to, value, fee, id, data, tags, signature, rawTx);
        }
        String signature = rawTx.optString(AR_KEY_SIGNATURE);
        String data = rawTx.optString(AR_KEY_DATA);
        return new ArweaveTransaction(from, to, value, fee, id, data, new Tag[0], signature, rawTx);
    }

    public static ArweaveTransaction fromRCC(JSONObject rccObject) throws JSONException {
        JSONObject json = rccObject.getJSONObject(AR_KEY_FORMATTED_JSON);
        JSONObject raw = rccObject.getJSONObject(AR_KEY_RAW_JSON);
        ArweaveTransaction arweaveTransaction = ArweaveTransaction.fromJSON(raw);
        arweaveTransaction.setSignatureData(json.getString(AR_KEY_SIGNATURE_DATA));
        return arweaveTransaction;
    }

    public ArweaveTransaction(String from, String to, String value, String fee, String id, String data, Tag[] tags, String signature, JSONObject rawTx) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.id = id;
        this.data = data;
        this.tags = tags;
        this.signature = signature;
        this.rawTx = rawTx;
        this.signatureData = null;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public String getFee() {
        return fee;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public List<Tag> getTags() {
        return Arrays.asList(tags);
    }

    public String getSignature() {
        return signature;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public JSONObject getRawTx() {
        return rawTx;
    }

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
    }

    public static class Tag {
        private final String name;
        private final String value;

        public Tag(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        static Tag[] fromJSONArray(JSONArray tags) throws JSONException {
            int len = tags.length();
            Tag[] result = new Tag[len];
            for (int i = 0; i < len; i++) {
                JSONObject tag = tags.getJSONObject(i);
                result[i] = new Tag(tag.getString("name"), tag.getString("value"));
            }
            return result;
        }

        @Override
        public String toString() {
            return "Tag{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
