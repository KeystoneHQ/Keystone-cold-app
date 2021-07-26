package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.Tuple;
import com.esaulpaugh.headlong.abi.TupleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CanonicalTupleValue extends CanonicalValue {
    List<CanonicalValue> elementTypes;
    CanonicalTupleValue(ABIType canonicalType) {
        super(canonicalType);
        TupleType tupleType = (TupleType)canonicalType;
        elementTypes = tupleType.elementTypes().stream().map(CanonicalValue::getCanonicalValue).collect(Collectors.toList());
    }

    @Override
    public void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        Tuple tuple = (Tuple) value;
        for (int i = 0; i < elementTypes.size(); i++) {
            Object subValue = tuple.get(i);
            JSONObject subObject = new JSONObject();
            CanonicalValue subType = elementTypes.get(i);
            subObject.put("name", subType.canonicalType.getName());
            subObject.put("type", subType.canonicalType.getCanonicalType());
            elementTypes.get(i).resolveValueToJSONObject(subValue, subObject);
            jsonArray.put(subObject);
        }
        jsonObject.put("type", "tuple");
        jsonObject.put("value", jsonArray);
    }
}
