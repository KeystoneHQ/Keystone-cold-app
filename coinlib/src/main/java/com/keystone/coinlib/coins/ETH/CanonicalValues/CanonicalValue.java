package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.esaulpaugh.headlong.abi.ABIType;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CanonicalValue {
    protected ABIType canonicalType;

    CanonicalValue(ABIType canonicalType) {
        this.canonicalType = canonicalType;
    }

    public abstract void resolveValueToJSONObject(Object value, JSONObject jsonObject) throws JSONException;

    public static CanonicalValue getCanonicalValue(ABIType abiType) {
        String canonicalType = abiType.getCanonicalType();
        if (canonicalType.endsWith("]")) return new CanonicalArrayValue(abiType);
        if (canonicalType.endsWith(")")) return new CanonicalTupleValue(abiType);
        if ("bool".equals(canonicalType)) return new CanonicalBoolValue(abiType);
        if (canonicalType.startsWith("int")) return new CanonicalIntValue(abiType);
        if (canonicalType.startsWith("uint")) return new CanonicalUIntValue(abiType);
        if (canonicalType.startsWith("fixed")) return new CanonicalFixedValue(abiType);
        if (canonicalType.startsWith("ufixed")) return new CanonicalUFixedValue(abiType);
        if ("address".equals(canonicalType)) return new CanonicalAddressValue(abiType);
        if ("string".equals(canonicalType)) return new CanonicalStringValue(abiType);
        if ("bytes".equals(canonicalType)) return new CanonicalBytesValue(abiType);
        if ("function".equals(canonicalType)) return new CanonicalFunctionValue(abiType);
        if (canonicalType.startsWith("bytes")) return new CanonicalBytesValue(abiType);
        throw new RuntimeException("Unknown type: " + canonicalType);
    }
}
