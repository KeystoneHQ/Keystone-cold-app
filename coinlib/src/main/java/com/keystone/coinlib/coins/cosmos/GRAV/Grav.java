package com.keystone.coinlib.coins.cosmos.GRAV;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Grav extends AbsCoin {

    protected Grav(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.GRAV.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "gravity";
        }
    }
}
