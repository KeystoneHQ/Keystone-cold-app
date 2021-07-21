package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalStringValue extends CanonicalValue {
    CanonicalStringValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
