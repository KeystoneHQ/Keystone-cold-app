package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.ArrayType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class CanonicalArrayValue extends CanonicalValue {
    CanonicalValue elementValue;

    CanonicalArrayValue(ABIType abiType) {
        super(abiType);
        ArrayType arrayType = (ArrayType) abiType;
        elementValue = CanonicalValue.getCanonicalValue(arrayType.getElementType());
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        switch (elementValue.canonicalType.typeCode()) {
            case ABIType.TYPE_CODE_BOOLEAN: {
                boolean[] valueArray = (boolean[]) value;
                for (Object subValues : valueArray) {
                    JSONObject subObject = new JSONObject();
                    elementValue.resolveValueToJSONObject(subValues, subObject);
                    jsonArray.put(subObject.opt("value"));
                }
                break;
            }
            case ABIType.TYPE_CODE_INT: {
                int[] valueArray = (int[]) value;
                for (Object subValues : valueArray) {
                    JSONObject subObject = new JSONObject();
                    elementValue.resolveValueToJSONObject(subValues, subObject);
                    jsonArray.put(subObject.opt("value"));
                }
                break;
            }
            case ABIType.TYPE_CODE_LONG: {
                long[] valueArray = (long[]) value;
                for (Object subValues : valueArray) {
                    JSONObject subObject = new JSONObject();
                    elementValue.resolveValueToJSONObject(subValues, subObject);
                    jsonArray.put(subObject.opt("value"));
                }
                break;
            }
            case ABIType.TYPE_CODE_BYTE: {
                byte[] valueArray = (byte[]) value;
                for (Object subValues : valueArray) {
                    JSONObject subObject = new JSONObject();
                    elementValue.resolveValueToJSONObject(subValues, subObject);
                    jsonArray.put(subObject.opt("value"));
                }
                break;
            }
            case ABIType.TYPE_CODE_BIG_INTEGER:
            case ABIType.TYPE_CODE_BIG_DECIMAL:
            case ABIType.TYPE_CODE_ARRAY:
            case ABIType.TYPE_CODE_TUPLE: {
                Object[] valueArray = (Object[]) value;
                for (Object subValues : valueArray) {
                    JSONObject subObject = new JSONObject();
                    elementValue.resolveValueToJSONObject(subValues, subObject);
                    jsonArray.put(subObject.opt("value"));
                }
            }

        }
        jsonObject.put("value", jsonArray);
    }
}
