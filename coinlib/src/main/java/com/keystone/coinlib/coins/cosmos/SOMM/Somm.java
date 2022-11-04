package com.keystone.coinlib.coins.cosmos.SOMM;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Somm extends AbsCoin {

    protected Somm(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.SOMM.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "somm";
        }
    }
}
