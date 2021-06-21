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
import com.keystone.coinlib.coins.XRP.XrpTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckCreate extends XrpTransaction {

    public CheckCreate() {
        super(Schemas.CheckCreate);
    }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            flatTransactionCommonFields(result, tx);
            result.putOpt("Destination", tx.opt("Destination"));
            result.putOpt("Expiration", formatTimeStamp(tx.optInt("Expiration")));
            result.putOpt("DestinationTag", tx.opt("DestinationTag"));
            result.putOpt("InvoiceID", tx.opt("InvoiceID"));
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
