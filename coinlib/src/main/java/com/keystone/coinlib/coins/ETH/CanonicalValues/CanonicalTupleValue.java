package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalTupleValue extends CanonicalValue {
    CanonicalTupleValue(String canonicalType) {
        super(canonicalType);
    }

    @Override
    public String resolveValueWith() {
        return null;
    }
}
