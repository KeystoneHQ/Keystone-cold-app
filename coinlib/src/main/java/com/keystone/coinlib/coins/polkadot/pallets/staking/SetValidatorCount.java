package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SetValidatorCount extends Pallet<SetValidatorCountParameter> {

    public SetValidatorCount(Network network, int code) {
        super("staking.setValidatorCount", network, code);
    }

    @Override
    public SetValidatorCountParameter read(ScaleCodecReader scr) {
        return new SetValidatorCountParameter(name, network, code, scr);
    }
}
