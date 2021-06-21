package com.keystone.coinlib.coins.polkadot.scale;

public class UnionValue<T> {

    private int index;
    private T value;

    public UnionValue(int index, T value) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative number: " + index);
        }
        if (index > 255) {
            throw new IllegalArgumentException("Union can have max 255 values. Index: " + index);
        }
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }
}
