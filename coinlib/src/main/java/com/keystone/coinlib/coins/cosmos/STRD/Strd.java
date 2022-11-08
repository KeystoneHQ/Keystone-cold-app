package com.keystone.coinlib.coins.cosmos.STRD;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Strd extends AbsCoin {

    protected Strd(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.STRD.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "stride";
        }
    }
}
