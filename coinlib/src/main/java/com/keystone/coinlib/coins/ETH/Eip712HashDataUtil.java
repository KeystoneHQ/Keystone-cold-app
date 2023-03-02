package com.keystone.coinlib.coins.ETH;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.keystone.coinlib.v8.ScriptLoader;


public class Eip712HashDataUtil {

    public static String typedDataHash(String message) {
        V8 v8 = ScriptLoader.sInstance.loadByCoinCode("ETH");
        V8Object coin = v8.executeObjectScript("new ETH()");
        v8.registerResource(coin);
        V8Function hashFunction = (V8Function) coin.get("typedDataHash");
        V8Array params = new V8Array(v8);
        params.push(message);
        String hashResult = (String) hashFunction.call(coin, params);
        if (hashResult.startsWith("0x")) {
            hashResult = hashResult.substring(2);
        }
        return hashResult;
    }
}
