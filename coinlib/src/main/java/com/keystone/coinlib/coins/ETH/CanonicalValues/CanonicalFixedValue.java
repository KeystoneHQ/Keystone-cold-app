package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalFixedValue extends CanonicalValue {
    CanonicalFixedValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
