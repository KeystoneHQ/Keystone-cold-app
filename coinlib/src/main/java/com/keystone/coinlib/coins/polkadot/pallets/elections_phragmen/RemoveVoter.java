package com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.EmptyParameter;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class RemoveVoter extends Pallet<EmptyParameter> {
    public RemoveVoter(Network network, int code) {
        super("electionsPhragmen.removeVoter", network, code);
    }

    @Override
    public EmptyParameter read(ScaleCodecReader scr) {
        return new EmptyParameter(name, network, code, scr);
    }
}
