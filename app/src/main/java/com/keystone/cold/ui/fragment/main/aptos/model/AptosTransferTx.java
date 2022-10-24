package com.keystone.cold.ui.fragment.main.aptos.model;

import com.keystone.cold.ui.fragment.main.aptos.model.payload.EntryFunction;

public class AptosTransferTx extends AptosTx {

    private String receiver;
    private String amount;

    public String getReceiver() {
        if (receiver != null) {
            return receiver;
        }
        if (getPayLoad() != null && getPayLoad() instanceof EntryFunction) {
            EntryFunction entryFunction = (EntryFunction) getPayLoad();
            receiver = entryFunction.getArguments().get(0);
        }
        return receiver;
    }

    public String getAmount() {
        if (amount != null) {
            return amount;
        }
        if (getPayLoad() != null && getPayLoad() instanceof EntryFunction) {
            EntryFunction entryFunction = (EntryFunction) getPayLoad();
            amount = entryFunction.getArguments().get(1);
        }
        return amount;
    }
}
