package com.keystone.coinlib.coins.cosmos.JUNO;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Juno extends AbsCoin {

    protected Juno(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.JUNO.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "juno";
        }
    }
}
