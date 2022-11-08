package com.keystone.coinlib.coins.cosmos.ATOM;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Atom extends AbsCoin {

    protected Atom(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.ATOM.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "cosmos";
        }
    }
}
