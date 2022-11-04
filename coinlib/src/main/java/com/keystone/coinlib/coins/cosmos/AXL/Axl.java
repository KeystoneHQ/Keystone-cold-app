package com.keystone.coinlib.coins.cosmos.AXL;


import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Axl extends AbsCoin {

    protected Axl(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.AXL.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "axelar";
        }
    }
}
