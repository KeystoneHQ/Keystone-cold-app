package com.keystone.coinlib.coins.polkadot.pallets.balance;

import com.keystone.coinlib.coins.polkadot.UOS.Network;

public class TransferKeepAlive extends TransferBase {
    public TransferKeepAlive(Network network, int code) {
        super("balance.transferKeepAlive", network, code);
    }
}
