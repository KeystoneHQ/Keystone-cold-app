package com.keystone.coinlib.coins.polkadot.pallets.utility;

import com.keystone.coinlib.coins.polkadot.UOS.Network;

public class BatchAll extends BatchBase {
    public BatchAll(Network network, int code) {
        super("utility.batchAll", network, code);
    }
}
