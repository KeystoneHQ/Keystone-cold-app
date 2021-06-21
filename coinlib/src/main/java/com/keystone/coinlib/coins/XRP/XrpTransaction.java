/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.XRP;

import android.text.TextUtils;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public abstract class XrpTransaction {

    protected String schema;
    private static final int decimals = 6;
    private static final long RippleEpochSeconds = 946684800L;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss 'UTC'",
            Locale.getDefault());

    protected XrpTransaction(String schema) {
        this.schema = schema;
    }

    public String getTransactionType() {
        return getClass().getSimpleName();
    }

    public abstract JSONObject flatTransactionDetail(JSONObject tx);

    public void flatTransactionCommonFields(JSONObject displayTx ,JSONObject tx)
    {
        try {
            displayTx.putOpt("TransactionType", tx.opt("TransactionType"));
            displayTx.putOpt("Account", tx.opt("Account"));
            displayTx.putOpt("Fee", formatAmount(tx.optString("Fee")));
            displayTx.putOpt("AccountTxnID", tx.opt("AccountTxnID"));
            //displayTx.putOpt("Flags", tx.opt("Flags"));
            displayTx.putOpt("SourceTag", tx.opt("SourceTag"));
            JSONArray Memo = tx.optJSONArray("Memos");
            if(null != Memo) {
                for( int index = 0; index < Memo.length(); index++) {
                    JSONObject MemoObj = Memo.optJSONObject(index);
                    JSONObject entry = MemoObj.optJSONObject("Memo");
                    if(entry != null && entry.has("MemoType") && entry.has("MemoData") ) {
                        if(Memo.length() > 1) {
                            displayTx.putOpt("Memo" + index + ".MemoData", formatMemo(entry.optString("MemoData")));
                            displayTx.putOpt("Memo" + index + ".MemoType", formatMemo(entry.optString("MemoType")));
                            displayTx.putOpt("Memo" + index + ".MemoFormat", formatMemo(entry.optString("MemoFormat")));
                        } else {
                            displayTx.putOpt("Memo" + ".MemoData", formatMemo(entry.optString("MemoData")));
                            displayTx.putOpt("Memo" + ".MemoType", formatMemo(entry.optString("MemoType")));
                            displayTx.putOpt("Memo" + ".MemoFormat", formatMemo(entry.optString("MemoFormat")));
                        }
                    }
                }
            }
            JSONArray Signer = tx.optJSONArray("Signers");
            if(null != Signer) {
                for( int index = 0; index < Signer.length(); index++) {
                    JSONObject SignerObj = Signer.optJSONObject(index);
                    JSONObject entry = SignerObj.optJSONObject("Signer");
                    if(entry!= null && entry.has("Account") && entry.has("TxnSignature") ) {
                        if(Signer.length() > 1) {
                            displayTx.putOpt("Signer" + index + ".Account", entry.opt("Account"));
                            displayTx.putOpt("Signer" + index + ".TxnSignature", entry.opt("TxnSignature"));
                            displayTx.putOpt("Signer" + index + ".SigningPubKey", entry.opt("SigningPubKey"));
                        } else {
                            displayTx.putOpt("Signer" + ".Account", entry.opt("Account"));
                            displayTx.putOpt("Signer" + ".TxnSignature", entry.opt("TxnSignature"));
                            displayTx.putOpt("Signer" + ".SigningPubKey", entry.opt("SigningPubKey"));
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String formatMemo(String hex) {
        if (TextUtils.isEmpty(hex)) return null;
        try {
            return new String(Hex.decode(hex));
        } catch (Exception e) {
            return null;
        }

    }

    public boolean isValid(JSONObject tx) {
        try {
            return getTransactionType().equals(tx.getString("TransactionType"))
                    && new JsonSchemaValidator().isStateValid(schema, tx.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected String formatAmount(String drops) {
        if(TextUtils.isEmpty(drops)) {
            return null;
        }
        try {
            return "XRP " + new BigDecimal(drops)
                    .divide(BigDecimal.TEN.pow(decimals), decimals, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return "0 XRP";
        }
    }

    public String formatTimeStamp(int time) {
        if(0 == time) {
            return null;
        }
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format((RippleEpochSeconds + time) * 1e3);
    }

    public static String formatDomain(String hexStr) {
        if (TextUtils.isEmpty(hexStr)) return null;
        try {
            return new String(Hex.decode(hexStr));
        } catch (Exception e) {
            return null;
        }
    }
    public static String formatCurrency(String hexStr) {
        if (TextUtils.isEmpty(hexStr)) return null;
        try {
            if(40 == hexStr.length()) {
                byte[] bytes = Hex.decode(hexStr);
                for (byte aByte : bytes) {
                    if ((aByte < 32 || aByte > 126) && aByte != 0) {
                        return hexStr;
                    }
                }
                return new String(bytes).replace("\u0000", "");
            } else {
                return hexStr;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
