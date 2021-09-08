package com.keystone.cold.viewmodel.tx;

public enum TransactionType {
    LEGACY(0x00),
    FEE_MARKET(0x02);

    private int type;

    TransactionType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
