package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class EmergencyCancel extends Pallet<EmergencyCancelParameter> {

    public EmergencyCancel(Network network, int code) {
        super("democracy.emergencyCancel", network, code);
    }

    @Override
    public EmergencyCancelParameter read(ScaleCodecReader scr) {
        return new EmergencyCancelParameter(name, network, code, scr);
    }
}
