package com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class RemoveMember extends Pallet<RemoveMemberParameter> {
    public RemoveMember(Network network, int code) {
        super("electionsPhragmen.removeMember", network, code);
    }

    @Override
    public RemoveMemberParameter read(ScaleCodecReader scr) {
        return new RemoveMemberParameter(name, network, code, scr);
    }
}
