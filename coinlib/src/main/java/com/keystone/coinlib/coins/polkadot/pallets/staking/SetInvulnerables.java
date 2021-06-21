package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SetInvulnerables extends Pallet<SetInvulnerablesParameter> {
    public SetInvulnerables(Network network, int code) {
        super("staking.setInvulnerables", network, code);
    }

    @Override
    public SetInvulnerablesParameter read(ScaleCodecReader scr) {
        return new SetInvulnerablesParameter(name, network, code, scr);
    }
}
