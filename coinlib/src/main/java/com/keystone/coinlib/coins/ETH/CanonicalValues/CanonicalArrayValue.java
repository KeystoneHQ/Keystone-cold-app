package com.keystone.coinlib.coins.ETH.CanonicalValues;

import org.json.JSONObject;

public class CanonicalArrayValue extends CanonicalValue {
    protected CanonicalValue elementValue;
    CanonicalArrayValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public void resolveValueWith(Object value, JSONObject jsonObject) {

    }
}
