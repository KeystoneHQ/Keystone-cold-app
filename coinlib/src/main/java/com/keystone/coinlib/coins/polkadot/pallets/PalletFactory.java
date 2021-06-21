package com.keystone.coinlib.coins.polkadot.pallets;

import com.keystone.coinlib.coins.polkadot.DOT.Dot;
import com.keystone.coinlib.coins.polkadot.KSM.Ksm;
import com.keystone.coinlib.coins.polkadot.UOS.Network;

public class PalletFactory {
    public static Pallet<? extends Parameter> getPallet(int code, Network network) {
        try {
            if (network.name.equals("Polkadot")) {
                return Dot.pallets.get(code);
            } else {
                return Ksm.pallets.get(code);
            }
        } catch (Exception e) {
            throw new Error("unknown pallet code");
        }
    }
}
