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

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentChannelClaim extends XrpTransaction {

    public PaymentChannelClaim() { super(Schemas.PaymentChannelClaim); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            flatTransactionCommonFields(result, tx);
            result.putOpt("Channel", tx.opt("Channel"));
            result.putOpt("Balance", formatAmount(tx.optString("Balance")));
            result.putOpt("Amount", formatAmount(tx.optString("Amount")));
            result.putOpt("Signature", tx.opt("Signature"));
            result.putOpt("PublicKey", tx.opt("PublicKey"));
            result.putOpt("Flags", TransactionFlagMap.getString(tx.optLong("Flags"),getTransactionType()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
