package com.keystone.coinlib.coins.ETH;

import android.util.Log;

import com.keystone.coinlib.abi.Contract;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class Erc20Handler implements FallbackHandler {
    private static final String TAG = "Erc20Handler";

    @Override
    public ABIReader.DecodedFunctionCall decodeCall(String data, Contract contract, String address) {
        contract.setAbi(readAsset("abi/Erc20.json"));
        contract.setName("Erc20");
        ABIReader abiReader = new ABIReader();
        return abiReader.decodeCall(data, contract, address);
    }

    @Override
    public void handleNestedContract(ABIReader.DecodedFunctionCall decodedFunctionCall) {
        Log.i(TAG, "handleNestedContract: there is currently no contract nesting");
    }
}
