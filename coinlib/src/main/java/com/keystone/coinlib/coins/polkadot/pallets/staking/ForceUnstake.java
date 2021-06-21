package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ForceUnstake extends Pallet<ForceUnstakeParameter> {

    public ForceUnstake(Network network, int code) {
        super("staking.forceUnstake", network, code);
    }

    @Override
    public ForceUnstakeParameter read(ScaleCodecReader scr) {
        return new ForceUnstakeParameter(name, network, code, scr);
    }
}
