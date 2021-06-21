package com.keystone.coinlib.coins.polkadot.pallets.balance;

import com.keystone.coinlib.coins.polkadot.UOS.Network;

public class Transfer extends TransferBase {
    public Transfer(Network network, int code) {
        super("balance.transfer", network, code);
    }
}
