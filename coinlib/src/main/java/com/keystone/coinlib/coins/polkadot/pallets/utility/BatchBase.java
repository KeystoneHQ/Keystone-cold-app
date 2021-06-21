package com.keystone.coinlib.coins.polkadot.pallets.utility;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class BatchBase extends Pallet<BatchParameter> {

    public BatchBase(String name, Network network, int code) {
        super(name, network, code);
    }

    @Override
    public BatchParameter read(ScaleCodecReader scr) {
        return new BatchParameter(network, name, code, scr);
    }
}
