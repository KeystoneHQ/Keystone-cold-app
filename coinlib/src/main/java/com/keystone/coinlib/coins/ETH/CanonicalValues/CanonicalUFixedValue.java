package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;

import org.json.JSONObject;

public class CanonicalUFixedValue extends CanonicalValue {
    CanonicalUFixedValue(ABIType abiType) {
        super(abiType);
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) {

    }

}
