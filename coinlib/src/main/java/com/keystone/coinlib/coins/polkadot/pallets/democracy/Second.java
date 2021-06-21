package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Second extends Pallet<SecondParameter> {
    public Second(Network network, int code) {
        super("democracy.second", network, code);
    }

    @Override
    public SecondParameter read(ScaleCodecReader scr) {
        return new SecondParameter(name, network, code, scr);
    }
}
