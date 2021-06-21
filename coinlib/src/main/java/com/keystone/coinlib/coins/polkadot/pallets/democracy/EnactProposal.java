package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class EnactProposal extends Pallet<EnactProposalParameter> {
    public EnactProposal(Network network, int code) {
        super("democracy.enactProposal", network, code);
    }

    @Override
    public EnactProposalParameter read(ScaleCodecReader scr) {
        return new EnactProposalParameter(name, network, code, scr);
    }
}
