package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.ArrayType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanonicalArrayValue extends CanonicalValue {
    CanonicalValue canonicalValue;

    CanonicalArrayValue(ABIType abiType) {
        super(abiType);
        ArrayType arrayType = (ArrayType) abiType;
        canonicalValue = CanonicalValue.getCanonicalValue(arrayType.getElementType());
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONArray valueArray;
        if (value instanceof JSONArray) {
            valueArray = (JSONArray)value;
        } else {
            valueArray = new JSONArray(value);
        }
        for (int i = 0; i < valueArray.length(); i++) {
            Object subValues = valueArray.get(i);
            JSONObject subObject = new JSONObject();
            canonicalValue.resolveValueToJSONObject(subValues, subObject);
            jsonArray.put(subObject.opt("value"));
        }
        jsonObject.put("value", jsonArray);
    }
}
