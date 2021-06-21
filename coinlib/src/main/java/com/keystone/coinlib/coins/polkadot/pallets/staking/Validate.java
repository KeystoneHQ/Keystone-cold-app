package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Validate extends Pallet<ValidateParameter> {
    public Validate(Network network, int code) {
        super("staking.validate", network, code);
    }

    @Override
    public ValidateParameter read(ScaleCodecReader scr) {
        return new ValidateParameter(name, network, code, scr);
    }
}
