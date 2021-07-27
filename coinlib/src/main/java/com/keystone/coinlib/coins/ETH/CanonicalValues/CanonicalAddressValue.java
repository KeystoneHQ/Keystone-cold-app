package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.keystone.coinlib.coins.ETH.ByteUtil;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class CanonicalAddressValue extends CanonicalValue {
    CanonicalAddressValue(ABIType abiType) {
        super(abiType);
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        jsonObject.put("value", "0x" + Hex.toHexString(ByteUtil.bigIntegerToBytes(new BigInteger(String.valueOf(value)), 20)));
    }
}
