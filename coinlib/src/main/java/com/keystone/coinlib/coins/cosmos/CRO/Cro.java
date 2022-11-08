package com.keystone.coinlib.coins.cosmos.CRO;


import com.keystone.coinlib.coins.AbsCoin;

import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Cro extends AbsCoin {

    protected Cro(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.CRO.coinCode();
    }


    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "cro";
        }
    }
}
