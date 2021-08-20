package com.keystone.coinlib.coins.ETH;

import com.keystone.coinlib.abi.Contract;

public interface FallbackHandler {
    ABIReader.DecodedFunctionCall decodeCall(String data, Contract contract, String address);

    void handleNestedContract(ABIReader.DecodedFunctionCall decodedFunctionCall);
}
