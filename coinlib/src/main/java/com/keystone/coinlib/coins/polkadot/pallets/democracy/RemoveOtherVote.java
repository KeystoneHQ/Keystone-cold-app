package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class RemoveOtherVote extends Pallet<RemoveOtherVoteParameter> {
    public RemoveOtherVote(Network network, int code) {
        super("democracy.removeOtherVote", network, code);
    }

    @Override
    public RemoveOtherVoteParameter read(ScaleCodecReader scr) {
        return new RemoveOtherVoteParameter(name, network, code, scr);
    }
}
