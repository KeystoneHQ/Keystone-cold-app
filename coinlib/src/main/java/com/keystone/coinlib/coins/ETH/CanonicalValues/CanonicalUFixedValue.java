package com.keystone.coinlib.coins.ETH.CanonicalValues;

public class CanonicalUFixedValue extends CanonicalValue {
    CanonicalUFixedValue(String abiType, Object value) {
        super(abiType, value);
    }

    @Override
    public String getValue() {
        return value.toString();
    }
}
