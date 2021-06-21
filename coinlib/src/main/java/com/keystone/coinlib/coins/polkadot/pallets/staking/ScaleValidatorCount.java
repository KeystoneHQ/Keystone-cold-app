package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ScaleValidatorCount extends Pallet<ScaleValidatorCountParameter> {

    public ScaleValidatorCount(Network network, int code) {
        super("staking.scaleValidatorCount", network, code);
    }

    @Override
    public ScaleValidatorCountParameter read(ScaleCodecReader scr) {
        return new ScaleValidatorCountParameter(name, network, code, scr);
    }
}
