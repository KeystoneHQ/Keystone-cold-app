package com.keystone.coinlib.coins.ETH.CanonicalValues;
import com.keystone.coinlib.coins.ETH.SolidityType;

public abstract class CanonicalValue {
    protected String canonicalType;
    protected Object value;

    CanonicalValue(String canonicalType, Object value) {
        this.canonicalType = canonicalType;
        this.value = value;
    }

    public abstract String getValue();

    public static CanonicalValue getCanonicalValue(String canonicalType, Object value) {
        if (canonicalType.endsWith("]")) return CanonicalArrayValue.getElementCanonicalValue(canonicalType);
        if ("bool".equals(typeName)) return new SolidityType.BoolType();
        if (typeName.startsWith("int")) return new SolidityType.IntType(typeName);
        if (typeName.startsWith("uint")) return new SolidityType.UnsignedIntType(typeName);
        if ("address".equals(typeName)) return new SolidityType.AddressType();
        if ("string".equals(typeName)) return new SolidityType.StringType();
        if ("bytes".equals(typeName)) return new SolidityType.BytesType();
        if ("function".equals(typeName)) return new SolidityType.FunctionType();
        if (typeName.startsWith("bytes")) return new SolidityType.Bytes32Type(typeName);
        throw new RuntimeException("Unknown type: " + typeName);
    }
}
