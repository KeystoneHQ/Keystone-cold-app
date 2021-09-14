/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.coins.XRP;

import com.keystone.coinlib.CoinImpl;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;

import org.json.JSONObject;

public class XrpImpl extends CoinImpl {

    @Override
    public String coinCode() {
        return Coins.XRP.coinCode();
    }

    public void generateJsonTransaction(JSONObject txObj, SignCallback callback, Signer... signers) {
        try {
            txObj.put("SigningPubKey", signers[0].getPublicKey());
            String txStr = txObj.toString();
            super.nativeLegacyGenerateTransaction(txStr,callback,signers[0], this.coinCode());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
    }
}
