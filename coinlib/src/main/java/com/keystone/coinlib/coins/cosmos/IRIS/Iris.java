package com.keystone.coinlib.coins.cosmos.IRIS;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Iris extends AbsCoin {

    protected Iris(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.IRIS.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "iaa";
        }
    }
}
