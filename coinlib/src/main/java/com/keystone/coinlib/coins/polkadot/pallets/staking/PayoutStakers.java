package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class PayoutStakers extends Pallet<PayoutStakersParameter> {

    public PayoutStakers(Network network, int code) {
        super("staking.payoutStakers", network, code);
    }

    @Override
    public PayoutStakersParameter read(ScaleCodecReader scr) {
        return new PayoutStakersParameter(name, network, code, scr);
    }
}
