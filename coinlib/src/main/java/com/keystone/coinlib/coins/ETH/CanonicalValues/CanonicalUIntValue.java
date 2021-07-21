package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalUIntValue extends CanonicalValue {
    CanonicalUIntValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
