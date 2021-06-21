package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class CancelReferendum extends Pallet<CancelReferendumParameter> {

    public CancelReferendum(Network network, int code) {
        super("democracy.cancelReferendum", network, code);
    }

    @Override
    public CancelReferendumParameter read(ScaleCodecReader scr) {
        return new CancelReferendumParameter(name, network, code, scr);
    }
}
