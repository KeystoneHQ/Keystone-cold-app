package com.keystone.coinlib.coins.polkadot.pallets;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public abstract class Pallet<T extends Parameter> {
    public String name;
    protected Network network;
    public int code;

    public Pallet(String name, Network network, int code) {
        this.name = name;
        this.network = network;
        this.code = code;
    }

    public abstract T read(ScaleCodecReader scr);
}
