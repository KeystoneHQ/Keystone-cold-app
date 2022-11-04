package com.keystone.coinlib.coins.cosmos.NGM;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Ngm extends AbsCoin {

    protected Ngm(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.NGM.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "emoney";
        }
    }
}
