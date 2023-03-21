package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model;

import android.text.TextUtils;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.Msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CosmosTx {

    public static CosmosTx from(String aminoJson) throws JSONException {
        JSONObject jsonObject = new JSONObject(aminoJson);
        String accountNumber = jsonObject.getString("account_number");
        String chainId = jsonObject.getString("chain_id");
        Fee fee = null;
        if (jsonObject.get("fee") instanceof JSONObject) {
             fee = Fee.from(jsonObject.getJSONObject("fee"));
        }
        String memo = jsonObject.getString("memo");
        JSONArray msgArray = jsonObject.getJSONArray("msgs");
        List<Msg> msgs = Msg.getMsgs(msgArray);
        String sequence = jsonObject.getString("sequence");
        return new CosmosTx(accountNumber, chainId, fee, memo, msgs, sequence);
    }

    public static String transformDirectToAmino(String directStr) throws JSONException {
        JSONObject directJsonObject = new JSONObject(directStr);
        String memo = directJsonObject.getJSONObject("body").getString("memo");
        JSONArray msgs = directJsonObject.getJSONObject("body").getJSONArray("msgs");
        String account_number = directJsonObject.optString("account_number");
        String chain_id = directJsonObject.optString("chain_id");
        JSONObject fee = directJsonObject.getJSONObject("auth_info").getJSONObject("fee");
        String sequence = null;
        JSONArray signer_infos = directJsonObject.getJSONObject("auth_info").getJSONArray("signer_infos");
        if (signer_infos.length() > 0) {
            sequence = signer_infos.getJSONObject(0).optString("sequence");
        }
        JSONObject aminoJson = new JSONObject();
        aminoJson.put("account_number", account_number);
        aminoJson.put("chain_id", chain_id);
        aminoJson.put("fee", fee);
        aminoJson.put("memo", memo);
        aminoJson.put("msgs", msgs);
        aminoJson.put("sequence", sequence);
        return aminoJson.toString();
    }

    private String accountNumber;
    private String chainId;
    private Fee fee;
    private String memo;
    private List<Msg> msgs;
    private String sequence;

    private String ur;

    public CosmosTx(String accountNumber, String chainId, Fee fee, String memo, List<Msg> msgs, String sequence) {
        this.accountNumber = accountNumber;
        this.chainId = chainId;
        this.fee = fee;
        this.memo = memo;
        this.msgs = msgs;
        this.sequence = sequence;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getChainId() {
        return chainId;
    }

    public Fee getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public List<Msg> getMsgs() {
        return msgs;
    }

    public String getSequence() {
        return sequence;
    }

    public String getCoinCode() {
        return Coins.getCosmosCoinCode(chainId);
    }

    public String getCoinName() {
        if (!TextUtils.isEmpty(chainId) && chainId.contains("9000")) {
            return "Evmos Testnet";
        }
        return Coins.coinNameFromCoinCode(Coins.getCosmosCoinCode(chainId));
    }

    public String getUr() {
        return ur;
    }

    public void setUr(String ur) {
        this.ur = ur;
    }

    @Override
    public String toString() {
        return "CosmosTx{" +
                "accountNumber='" + accountNumber + '\'' +
                ", chainId='" + chainId + '\'' +
                ", fee=" + fee +
                ", memo='" + memo + '\'' +
                ", msgs=" + msgs +
                ", sequence='" + sequence + '\'' +
                '}';
    }
}
