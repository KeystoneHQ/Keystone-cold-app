package com.keystone.coinlib.coins.cosmos.XPRT;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Xprt extends AbsCoin {

    protected Xprt(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.XPRT.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "persistence";
        }
    }
}
