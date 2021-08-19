package com.keystone.coinlib.coins.ETH;

import android.text.TextUtils;

import com.keystone.coinlib.abi.Contract;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class GnosisHandler implements FallbackHandler {
    @Override
    public ABIReader.DecodedFunctionCall decodeCall(String data, Contract contract) {
        contract.setAbi(readAsset("abi/Mastercopy_1.2.0.json"));
        contract.setName("Gnosis Safe: Mastercopy 1.2.0");
        ABIReader abiReader = new ABIReader();
        return abiReader.decodeCall(data, contract);
    }

    @Override
    public void handleNestedContract(ABIReader.DecodedFunctionCall decodedFunctionCall) {
        if (TextUtils.equals(decodedFunctionCall.function.getName(), "createProxyWithNonce")) {
            Object _singleton = decodedFunctionCall.callParameters.get(0);
            String address = "0x" + Hex.toHexString(
                    ByteUtil.bigIntegerToBytes(new BigInteger(String.valueOf(_singleton)), 20));
            Contract contract = EthImpl.getContract(null, address);
            Object initializer = decodedFunctionCall.callParameters.get(1);
            String data = Hex.toHexString((byte[]) initializer);
            ABIReader.DecodedFunctionCall call = ABIReader.staticDecodeCall(data);
            if (call != null) {
                data = call.toJson().toString();
            }
            ABIReader abiReader = new ABIReader();
            abiReader.decodeCall(data, contract);
        }
    }
}
