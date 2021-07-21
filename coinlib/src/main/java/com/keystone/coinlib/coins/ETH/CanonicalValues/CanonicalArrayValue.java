package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;

import org.json.JSONObject;

public class CanonicalArrayValue extends CanonicalValue {
    protected CanonicalValue elementValue;
    CanonicalArrayValue(ABIType canonicalType) {
        super(canonicalType);

    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) {

    }
}
