package com.keystone.coinlib.coins.ETH.CanonicalValues;

import com.keystone.coinlib.coins.ETH.SolidityType;

public class CanonicalArrayValue extends CanonicalValue {
    protected CanonicalValue elementValue;
    CanonicalArrayValue(String canonicalType, Object value, CanonicalValue elementValue) {
        super(canonicalType, value);
        this.elementValue = elementValue;
    }

    @Override
    public String getValue() {
        return null;
    }

    public static CanonicalArrayValue getCanonicalValue(String canonicalType, Object value) {
        return new CanonicalArrayValue(canonicalType, value, CanonicalValue.getCanonicalValue(canonicalType.substring(0, canonicalType.lastIndexOf("["))));
    }
}
