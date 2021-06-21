package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class RemoveVote extends Pallet<RemoveVoteParameter> {
    public RemoveVote(Network network, int code) {
        super("democracy.removeVote", network, code);
    }

    @Override
    public RemoveVoteParameter read(ScaleCodecReader scr) {
        return new RemoveVoteParameter(name, network, code, scr);
    }
}
