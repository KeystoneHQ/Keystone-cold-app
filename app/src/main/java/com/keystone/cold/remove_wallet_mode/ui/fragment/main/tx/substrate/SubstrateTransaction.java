package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;

import org.json.JSONException;
import org.json.JSONObject;

public class SubstrateTransaction {
    private String type;
    private String coinCode;
    private JSONObject parsedTransaction;
    private String rawHex;
    private int checksum;

    private String signedHex;

    public SubstrateTransaction(String type, String coinCode, JSONObject parsedTransaction, String rawHex, int checksum) {
        // Sign, Stub, Read
        this.type = type;
        // null if type is not Sign
        this.coinCode = coinCode;
        this.parsedTransaction = parsedTransaction;
        this.rawHex = rawHex;
        // 0 if type is not Sign or Stub
        this.checksum = checksum;
    }

    public static SubstrateTransaction factory(String jsonStr, String rawHex) throws JSONException, InvalidTransactionException {
        JSONObject pt = new JSONObject(jsonStr);
        String status = pt.optString("status", "success");
        if (status.equals("failed")) throw new InvalidTransactionException(MainApplication.getApplication().getString(R.string.incorrect_tx_data), "invalid transaction");
        String type = pt.getString("transaction_type");
        int checksum = pt.optInt("checksum", 0);
        String coinCode = null;
        if (type.equals("Sign")) {
            JSONObject networkInfo = pt.getJSONObject("network_info");
            String network = networkInfo.getString("network_title");
            switch (network) {
                case "Polkadot": {
                    coinCode = Coins.DOT.coinCode();
                    break;
                }
                case "Kusama": {
                    coinCode = Coins.KSM.coinCode();
                    break;
                }
                default: {
                    coinCode = Coins.DOT.coinCode();
                }
            }
        }
        return new SubstrateTransaction(type, coinCode, pt, rawHex, checksum);
    }

    public int getChecksum() {
        return checksum;
    }

    public String getType() {
        return type;
    }

    public JSONObject getParsedTransaction() {
        return parsedTransaction;
    }

    public String getRawHex() {
        return rawHex;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public String getSignedHex() {
        return signedHex;
    }

    public void setSignedHex(String signedHex) {
        this.signedHex = signedHex;
    }
}
