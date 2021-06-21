package com.keystone.coinlib.coins.polkadot.pallets.proxy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class AddProxy extends Pallet<AddProxyParameter> {
    public AddProxy(Network network, int code) {
        super("proxy.addProxy", network, code);
    }

    @Override
    public AddProxyParameter read(ScaleCodecReader scr) {
        return new AddProxyParameter(name, network, code, scr);
    }
}
