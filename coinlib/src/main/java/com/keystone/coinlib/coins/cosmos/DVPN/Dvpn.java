package com.keystone.coinlib.coins.cosmos.DVPN;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Dvpn extends AbsCoin {

    protected Dvpn(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.DVPN.coinCode();
    }


    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "sent";
        }
    }
}
