package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalIntValue extends CanonicalValue {
    CanonicalIntValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
