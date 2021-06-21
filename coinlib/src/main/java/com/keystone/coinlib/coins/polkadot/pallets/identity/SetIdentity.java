package com.keystone.coinlib.coins.polkadot.pallets.identity;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SetIdentity extends Pallet<SetIdentityParameter> {
    public SetIdentity(Network network, int code) {
        super("identity.setIdentity", network, code);
    }

    @Override
    public SetIdentityParameter read(ScaleCodecReader scr) {
        return new SetIdentityParameter(name, network, code, scr);
    }
}
