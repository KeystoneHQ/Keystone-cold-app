package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.ArrayType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanonicalArrayValue extends CanonicalValue {
    int count;
    CanonicalValue canonicalValue;

    CanonicalArrayValue(ABIType abiType) {
        super(abiType);
        ArrayType arrayType = (ArrayType) abiType;
        count = getCount(abiType.getCanonicalType());
        canonicalValue = CanonicalValue.getCanonicalValue(arrayType.getElementType());
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        if (count == 1) {
            JSONArray jsonArray = new JSONArray();
            Object[] objects = (Object[]) value;
            for (int i = 0; i < objects.length; i++) {
                Object subValue = objects[i];
                JSONObject subObject = new JSONObject();
                canonicalValue.resolveValueToJSONObject(subValue, subObject);
                jsonArray.put(subObject.optString("value"));
            }
            jsonObject.put("value", jsonArray);
        } else if (count == 2) {
            JSONArray jsonArray = new JSONArray();
            Object[][] objects = (Object[][]) value;
            for (int i = 0; i < objects.length; i++) {
                Object[] subValues = objects[i];
                JSONObject subObject = new JSONObject();
                canonicalValue.resolveValueToJSONObject(subValues, subObject);
                jsonArray.put(subObject.optJSONArray("value"));
            }
            jsonObject.put("value", jsonArray);
        }
    }

    public static int getCount(String canonicalType) {
        int length = canonicalType.length();
        canonicalType = canonicalType.replace("]", "");
        return length - canonicalType.length();
    }
}
