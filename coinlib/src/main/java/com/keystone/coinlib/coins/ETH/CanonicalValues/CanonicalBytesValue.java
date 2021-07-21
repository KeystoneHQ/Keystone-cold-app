package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalBytesValue extends CanonicalValue {
    CanonicalBytesValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
