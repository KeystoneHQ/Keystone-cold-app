package com.keystone.coinlib.coins.cosmos.TGD;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Tgd extends AbsCoin {

    protected Tgd(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.TGD.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "tgrade";
        }
    }
}
