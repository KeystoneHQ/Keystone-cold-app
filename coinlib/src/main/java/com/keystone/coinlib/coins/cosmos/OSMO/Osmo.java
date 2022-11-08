package com.keystone.coinlib.coins.cosmos.OSMO;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Osmo extends AbsCoin {

    protected Osmo(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.OSMO.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "osmo";
        }
    }
}
