package com.keystone.coinlib.coins.cosmos.UMEE;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Umee extends AbsCoin {

    protected Umee(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.UMEE.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "umee";
        }
    }
}
