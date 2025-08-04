package com.keystone.coinlib.coins.cosmos.TERRA;

import android.util.Log;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.cosmos.AddressCodec;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.coins.cosmos.PublicKeyHelper;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Terra extends AbsCoin {

    protected Terra(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.Terra.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "terra";
        }
    }
}
