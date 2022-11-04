package com.keystone.coinlib.coins.cosmos.AKT;

import android.util.Log;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.cosmos.AddressCodec;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.coins.cosmos.PublicKeyHelper;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Akt extends AbsCoin {

    protected Akt(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.AKT.coinCode();
    }

    public static class Deriver extends CosmosDeriver {
        public Deriver() {
            prefix = "akash";
        }
    }
}
