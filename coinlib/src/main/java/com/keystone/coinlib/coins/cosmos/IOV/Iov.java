package com.keystone.coinlib.coins.cosmos.IOV;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Iov extends AbsCoin {

    protected Iov(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.IOV.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "star";
        }
    }
}
