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

package com.keystone.coinlib.coins.BTC;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.v8.CoinImpl;
import com.eclipsesource.v8.V8Object;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.Stream;

public class BtcImpl extends CoinImpl {
    private V8Function parsePsbt;
    private V8Function signPsbt;

    public BtcImpl() {
        super("BTC");
    }

    SignTxResult generateOmniTx(@NonNull AbsTx tx, Signer... signers) {
        V8Object txData = constructTxData(tx.getMetaData());
        return signTxImpl(txData, "generateOmniTransactionSync", signers);
    }

    public SignPsbtResult signPsbt(@NonNull String psbt, boolean finalize, Signer... signers) {
        if (this.signPsbt == null) {
            this.signPsbt = (V8Function) coin.get("signPSBTBase64Sync");
        }
        v8.registerResource(signPsbt);

        V8Array params = new V8Array(v8);
        v8.registerResource(params);
        params.push(psbt);

        if (signers.length > 0) {
            V8Array signProviders = new V8Array(v8);
            v8.registerResource(signProviders);
            Stream.of(signers).forEach(signer -> signProviders.push(createSignerProvider(signer)));
            params.push(signProviders);
        } else {
            return null;
        }
        params.push(finalize);
        try {
            V8Object object = (V8Object) signPsbt.call(coin, params);
            return new SignPsbtResult(object.getString("txId"),
                    object.getString("psbtB64"));
        } catch (V8ScriptExecutionException e) {
            e.printStackTrace();
        } finally {
            v8.release(false);
        }
        return null;
    }

    public JSONObject parsePsbt(@NonNull String psbtBase64) throws JSONException {
        if (this.parsePsbt == null) {
            this.parsePsbt = (V8Function) coin.get("parsePsbt");
        }
        v8.registerResource(parsePsbt);
        V8Array params = new V8Array(v8);
        v8.registerResource(params);
        params.push(psbtBase64);
        try {
            V8Object result = (V8Object) parsePsbt.call(coin, params);
            v8.registerResource(result);

            V8Object json = v8.getObject("JSON");
            v8.registerResource(json);

            V8Array parameters = new V8Array(v8).push(result);
            v8.registerResource(parameters);
            String jsonResult = json.executeStringFunction("stringify", parameters);
            return new JSONObject(jsonResult);
        } catch (V8ScriptExecutionException | JSONException e) {
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        } finally {
            v8.release(false);
        }
    }
}
