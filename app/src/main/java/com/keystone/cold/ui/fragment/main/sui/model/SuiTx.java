package com.keystone.cold.ui.fragment.main.sui.model;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.ui.fragment.main.aptos.model.payload.PayLoad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SuiTx {

    private static final String ADDRESS_PREFIX = "0x";

    private long chainId;
    private String sender;
    private PayLoad payLoad;
    private long gasUnitPrice;
    private long maxGasAmount;
    private long expirationTimestampSecs;

    private String signatureUR;

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public String getSender() {
        if (sender != null && !sender.startsWith(ADDRESS_PREFIX)) {
            return ADDRESS_PREFIX + sender;
        }
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public PayLoad getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(PayLoad payLoad) {
        this.payLoad = payLoad;
    }

    public long getGasUnitPrice() {
        return gasUnitPrice;
    }

    public void setGasUnitPrice(long gasUnitPrice) {
        this.gasUnitPrice = gasUnitPrice;
    }

    public long getMaxGasAmount() {
        return maxGasAmount;
    }

    public void setMaxGasAmount(long maxGasAmount) {
        this.maxGasAmount = maxGasAmount;
    }

    public long getExpirationTimestampSecs() {
        return expirationTimestampSecs;
    }

    public void setExpirationTimestampSecs(long expirationTimestampSecs) {
        this.expirationTimestampSecs = expirationTimestampSecs;
    }

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
    }

    @Override
    public String toString() {
        return "SuiTx{" +
                "chainId=" + chainId +
                ", sender='" + sender + '\'' +
                ", payLoad=" + payLoad +
                ", gasUnitPrice=" + gasUnitPrice +
                ", maxGasAmount=" + maxGasAmount +
                ", expirationTimestampSecs=" + expirationTimestampSecs +
                '}';
    }

    public static SuiTx fromRaw(JSONObject raw) {
        SuiTx suiTx = new SuiTx();
        suiTx.setChainId(Coins.SUI.coinIndex());
        PayLoad payLoad = new PayLoad();
        try {
            JSONObject v1 = raw.getJSONObject("V1");
            JSONObject kind = v1.getJSONObject("kind");
            JSONObject programmableTransaction = kind.getJSONObject("ProgrammableTransaction");
            JSONArray commands = programmableTransaction.getJSONArray("commands");
            String type = "";
            for (int i = 0; i < commands.length(); i++) {
                type += (i > 0 ? "+" : "") + commands.getJSONObject(i).keys().next();
            }
            payLoad.setType(type);
            payLoad.setRawJson(raw.toString());
            suiTx.setPayLoad(payLoad);
            suiTx.setSender(v1.getString("sender"));
            JSONObject gasData = v1.getJSONObject("gas_data");
            suiTx.setGasUnitPrice(gasData.getLong("price"));
            suiTx.setMaxGasAmount(gasData.getLong("budget"));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return suiTx;
    }
}
