package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.keystone.coinlib.coins.ETH.ABIReader;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

public class CanonicalBytesValue extends CanonicalValue {
    CanonicalBytesValue(ABIType canonicalType) {
        super(canonicalType);
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        String valueString = Hex.toHexString((byte[]) value);
        valueString = tryToDecodeBytes(valueString);
        jsonObject.put("value", valueString);
    }


    private String tryToDecodeBytes(String valueString) {
        String value = valueString;
        ABIReader.DecodedFunctionCall decodedFunctionCall = ABIReader.staticDecodeCall(valueString);
        if (decodedFunctionCall != null) {
            value = decodedFunctionCall.toJson().toString();
        }
        return value;
    }
}
