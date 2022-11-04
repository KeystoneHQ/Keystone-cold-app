package com.keystone.coinlib.coins.cosmos.CTK;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Ctk extends AbsCoin {

    protected Ctk(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.CTK.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "certik";
        }
    }
}
