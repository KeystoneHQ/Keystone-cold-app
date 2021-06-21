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

package com.keystone.coinlib.coins.XRP.transcationtype;

import com.keystone.coinlib.coins.XRP.Schemas;
import com.keystone.coinlib.coins.XRP.TransactionFlagMap;
import com.keystone.coinlib.coins.XRP.XrpTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Payment extends XrpTransaction {

    public Payment() { super(Schemas.Payment); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            flatTransactionCommonFields(result, tx);
            result.putOpt("Destination", tx.opt("Destination"));
            result.putOpt("DestinationTag", tx.opt("DestinationTag"));
            result.putOpt("InvoiceID", tx.opt("InvoiceID"));
            result.putOpt("Flags", TransactionFlagMap.getString(tx.optLong("Flags"),getTransactionType()));
            if(tx.has("Amount")){
                if(null != tx.optJSONObject("Amount")) {
                    JSONObject amount = tx.optJSONObject("Amount");
                    if(amount != null && amount.has("value")
                            && amount.has("currency") && amount.has("issuer")) {
                        result.putOpt("Amount.value", amount.opt("value"));
                        result.putOpt("Amount.currency", formatCurrency(amount.optString("currency")));
                        result.putOpt("Amount.issuer", amount.opt("issuer"));
                    }
                } else {
                    result.putOpt("Amount", formatAmount(tx.optString("Amount")));
                }
            }
            if(tx.has("SendMax")){
                if(null != tx.optJSONObject("SendMax")) {
                    JSONObject amount = tx.optJSONObject("SendMax");
                    if(amount != null && amount.has("value")
                            && amount.has("currency") && amount.has("issuer")) {
                        result.putOpt("SendMax.value", amount.opt("value"));
                        result.putOpt("SendMax.currency", formatCurrency(amount.optString("currency")));
                        result.putOpt("SendMax.issuer", amount.opt("issuer"));
                    }
                } else {
                    result.putOpt("SendMax", formatAmount(tx.optString("SendMax")));
                }
            }
            if(tx.has("DeliverMin")) {
                if(null != tx.optJSONObject("DeliverMin")) {
                    JSONObject amount = tx.optJSONObject("DeliverMin");
                    if(amount != null && amount.has("value")
                            && amount.has("currency") && amount.has("issuer")) {
                        result.putOpt("DeliverMin.value", amount.opt("value"));
                        result.putOpt("DeliverMin.currency", formatCurrency(amount.optString("currency")));
                        result.putOpt("DeliverMin.issuer", amount.opt("issuer"));
                    }
                } else {
                    result.putOpt("DeliverMin", formatAmount(tx.optString("DeliverMin")));
                }
            }

            JSONArray pathArray = tx.optJSONArray("Paths");
            if(pathArray != null){
                result.putOpt("Paths", pathArray.toString(2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
