package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class CanonicalUFixedValue extends CanonicalValue {
    CanonicalUFixedValue(ABIType abiType) {
        super(abiType);
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        jsonObject.put("value", ((BigDecimal)value).toString());
    }

}
