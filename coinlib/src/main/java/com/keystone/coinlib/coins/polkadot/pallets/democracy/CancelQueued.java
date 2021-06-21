package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class CancelQueued extends Pallet<CancelQueuedParameter> {

    public CancelQueued(Network network, int code) {
        super("democracy.cancelQueued", network, code);
    }

    @Override
    public CancelQueuedParameter read(ScaleCodecReader scr) {
        return new CancelQueuedParameter(name, network, code, scr);
    }
}
