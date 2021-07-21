package com.keystone.coinlib.coins.ETH.CanonicalValues;

import org.json.JSONObject;

public abstract class CanonicalValue {
    protected String canonicalType;

    CanonicalValue(String canonicalType) {
        this.canonicalType = canonicalType;
    }

    public abstract void resolveValueWith(Object value, JSONObject jsonObject);

    public static CanonicalValue getCanonicalValue(String canonicalType) {
        if (canonicalType.endsWith("]")) return new CanonicalArrayValue(canonicalType);
        if ("bool".equals(canonicalType)) return new CanonicalBoolValue(canonicalType);
        if (canonicalType.startsWith("int")) return new CanonicalIntValue(canonicalType);
        if (canonicalType.startsWith("uint")) return new CanonicalUIntValue(canonicalType);
        if (canonicalType.startsWith("fixed")) return new CanonicalFixedValue(canonicalType);
        if (canonicalType.startsWith("ufixed")) return new CanonicalUFixedValue(canonicalType);
        if ("address".equals(canonicalType)) return new CanonicalAddressValue(canonicalType);
        if ("string".equals(canonicalType)) return new CanonicalStringValue(canonicalType);
        if ("bytes".equals(canonicalType)) return new CanonicalBytesValue(canonicalType);
        if (canonicalType.startsWith("bytes")) return new CanonicalBytesValue(canonicalType);
        throw new RuntimeException("Unknown type: " + canonicalType);
    }
}
