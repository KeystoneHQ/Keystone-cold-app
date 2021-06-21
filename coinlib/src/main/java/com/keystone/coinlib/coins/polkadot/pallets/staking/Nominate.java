package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Nominate extends Pallet<NominateParameter> {
    public Nominate(Network network, int code) {
        super("staking.nominate", network, code);
    }

    @Override
    public NominateParameter read(ScaleCodecReader scr) {
        return new NominateParameter(name, network, code, scr);
    }
}