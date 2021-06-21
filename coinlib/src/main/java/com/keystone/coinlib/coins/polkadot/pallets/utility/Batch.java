package com.keystone.coinlib.coins.polkadot.pallets.utility;

import com.keystone.coinlib.coins.polkadot.UOS.Network;

public class Batch extends BatchBase {
    public Batch(Network network, int code) {
        super("utility.batch", network, code);
    }
}
