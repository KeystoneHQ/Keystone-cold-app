package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalBoolValue extends CanonicalValue {
    CanonicalBoolValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
