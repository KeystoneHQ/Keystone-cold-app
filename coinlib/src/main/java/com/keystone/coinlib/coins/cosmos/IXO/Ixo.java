package com.keystone.coinlib.coins.cosmos.IXO;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Ixo extends AbsCoin {

    protected Ixo(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.IXO.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "ixo";
        }
    }
}
