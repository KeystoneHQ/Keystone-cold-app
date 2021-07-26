package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.ArrayType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CanonicalArrayValue extends CanonicalValue {
    int count;
    List<CanonicalValue> canonicalValuesOne;
    CanonicalValue[][] canonicalValuesTwo;
    CanonicalArrayValue(ABIType abiType) {
        super(abiType);
        ArrayType arrayType = (ArrayType) abiType;
        count = getCount(abiType.getCanonicalType());
        if (count == 1) {
            canonicalValuesOne = Stream.of(arrayType.getElementType()).map(CanonicalValue::getCanonicalValue).collect(Collectors.toList());
        } else if (count == 2) {
            canonicalValuesTwo = Stream.of(arrayType.getElementType()).map(CanonicalValue::getCanonicalValue).toArray(CanonicalValue[][]::new);
        }
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        if (count == 1) {
            JSONArray jsonArray = new JSONArray();
            Object[] objects = (Object[]) value;
            CanonicalValue canonicalValue = canonicalValuesOne.get(0);
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
            for (int i = 0; i < canonicalValuesTwo.length; i++) {
                JSONArray jsonArrayj = new JSONArray();
                for (int j = 0; j < canonicalValuesTwo[i].length; j++) {
                    Object subValue = objects[i];
                    JSONObject subObject = new JSONObject();
                    CanonicalValue subType = canonicalValuesTwo[i][j];
                    subObject.put("name", subType.canonicalType.getName());
                    subObject.put("type", subType.canonicalType.getCanonicalType());
                    canonicalValuesTwo[i][j].resolveValueToJSONObject(subValue, subObject);
                    jsonArrayj.put(subObject);
                }
                jsonArray.put(jsonArrayj);
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
