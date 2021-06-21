package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ExternalProposeMajority extends Pallet<ExternalProposeParameter> {

    public ExternalProposeMajority(Network network, int code) {
        super("democracy.externalProposeMajority", network, code);
    }

    @Override
    public ExternalProposeParameter read(ScaleCodecReader scr) {
        return new ExternalProposeParameter(name, network, code, scr);
    }
}
