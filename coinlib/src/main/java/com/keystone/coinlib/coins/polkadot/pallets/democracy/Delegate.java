package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Delegate extends Pallet<DelegateParameter> {

    public Delegate(Network network, int code) {
        super("democracy.delegate", network, code);
    }

    @Override
    public DelegateParameter read(ScaleCodecReader scr) {
        return new DelegateParameter(name, network, code, scr);
    }
}
