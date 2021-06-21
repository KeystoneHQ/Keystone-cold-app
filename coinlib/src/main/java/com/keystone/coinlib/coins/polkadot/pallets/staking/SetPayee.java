package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SetPayee extends Pallet<SetPayeeParameter> {
    public SetPayee(Network network, int code) {
        super("staking.setPayee", network, code);
    }

    @Override
    public SetPayeeParameter read(ScaleCodecReader scr) {
        return new SetPayeeParameter(name, network, code, scr);
    }
}
