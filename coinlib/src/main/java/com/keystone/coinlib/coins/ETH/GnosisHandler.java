package com.keystone.coinlib.coins.ETH;

import android.text.TextUtils;

import com.keystone.coinlib.abi.Contract;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

public class GnosisHandler implements FallbackHandler {
    public static List<String> gnosisContractAddresses = new ArrayList<>();

    @Override
    public ABIReader.DecodedFunctionCall decodeCall(String data, Contract contract, String address) {
        contract.setAbi(readAsset("abi/Safe_Singleton_L2_1.3.0.json"));
        contract.setName("Safe");
        ABIReader abiReader = new ABIReader();
        ABIReader.DecodedFunctionCall call = abiReader.decodeCall(data, contract, address);
        if (call != null) {
            gnosisContractAddresses.add(address.toLowerCase());
        }
        return call;
    }

    @Override
    public void handleNestedContract(ABIReader.DecodedFunctionCall decodedFunctionCall) {
        if (TextUtils.equals(decodedFunctionCall.function.getName(), "createProxyWithNonce")) {
            decodeCreateProxyWithNonce(decodedFunctionCall);
        } else if (TextUtils.equals(decodedFunctionCall.function.getName(), "execTransaction")) {
            decodeExecTransaction(decodedFunctionCall);
        }
    }

    private void decodeExecTransaction(ABIReader.DecodedFunctionCall decodedFunctionCall) {
        Object _to = decodedFunctionCall.callParameters.get(0);
        String address = "0x" + Hex.toHexString(
                ByteUtil.bigIntegerToBytes(new BigInteger(String.valueOf(_to)), 20));
        Object _data = decodedFunctionCall.callParameters.get(2);
        String data = Hex.toHexString((byte[]) _data);
        decode(address, data);
    }

    private void decodeCreateProxyWithNonce(ABIReader.DecodedFunctionCall decodedFunctionCall) {
        Object _singleton = decodedFunctionCall.callParameters.get(0);
        String address = "0x" + Hex.toHexString(
                ByteUtil.bigIntegerToBytes(new BigInteger(String.valueOf(_singleton)), 20));
        Object initializer = decodedFunctionCall.callParameters.get(1);
        String data = Hex.toHexString((byte[]) initializer);
        decode(address, data);
    }

    private void decode(String address, String data){
        ABIReader abiReader = new ABIReader();
        List<Contract> contracts = EthImpl.getContract(address);
        for (Contract contract: contracts){
            abiReader.decodeCall(data, contract, "");
        }
    }
}
