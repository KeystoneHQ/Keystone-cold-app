package com.keystone.coinlib.coins.cosmos.SCRT;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Scrt extends AbsCoin {

    protected Scrt(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.SCRT.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "secret";
        }
    }
}
