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

public class PaymentChannelCreate extends XrpTransaction {

    public PaymentChannelCreate() { super(Schemas.PaymentChannelCreate); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            flatTransactionCommonFields(result, tx);
            result.putOpt("Amount", formatAmount(tx.optString("Amount")));
            result.putOpt("Destination", tx.opt("Destination"));
            result.putOpt("DestinationTag", tx.opt("DestinationTag"));
            result.putOpt("SettleDelay", tx.opt("SettleDelay"));
            result.putOpt("PublicKey", tx.opt("PublicKey"));
            result.putOpt("CancelAfter", formatTimeStamp(tx.optInt("CancelAfter")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
