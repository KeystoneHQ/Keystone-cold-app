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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignerListSet extends XrpTransaction {

    public SignerListSet() { super(Schemas.SignerListSet); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            flatTransactionCommonFields(result, tx);
            result.putOpt("SignerQuorum", tx.opt("SignerQuorum"));
            JSONArray Signer = tx.optJSONArray("SignerEntries");
            if(null != Signer) {
                for( int index = 0; index < Signer.length(); index++){
                    JSONObject SignerObj = Signer.optJSONObject(index);
                    JSONObject entry = SignerObj.optJSONObject("SignerEntry");
                    if(entry != null && entry.has("Account") && entry.has("SignerWeight") ) {
                        if (Signer.length() > 1) {
                            result.putOpt("SignerEntry"+ index +".Account", entry.opt("Account"));
                            result.putOpt("SignerEntry"+ index +".SignerWeight", entry.opt("SignerWeight"));
                        } else {
                            result.putOpt("SignerEntry" +".Account", entry.opt("Account"));
                            result.putOpt("SignerEntry" +".SignerWeight", entry.opt("SignerWeight"));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
