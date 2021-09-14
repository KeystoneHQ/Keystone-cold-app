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

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;

import org.json.JSONObject;

public class XrpImpl implements Coin {
    private static native Void nativeGenerateTransaction(final String tx, SignCallback sc, Signer signer);
    private static native Void nativeGenerateJsonTransaction(final String tx, SignCallback sc, Signer signer);

    static {
        System.loadLibrary("CryptoCoinKitXRP");
    }

    @Override
    public String coinCode() {
        return Coins.XRP.coinCode();
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject txObj = tx.getMetaData();
        try {
            txObj.put("signingPubKey", signers[0].getPublicKey());
            String txStr = txObj.toString();
            nativeGenerateTransaction(txStr,callback,signers[0]);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return null;
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return null;
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return false;
    }

    public void generateJsonTransaction(JSONObject txObj, SignCallback callback, Signer... signers) {
        try {
            txObj.put("SigningPubKey", signers[0].getPublicKey());
            String txStr = txObj.toString();
            nativeGenerateJsonTransaction(txStr,callback,signers[0]);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
    }
}
