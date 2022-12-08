package com.keystone.coinlib.coins.AR;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

public class Arweave extends AbsCoin {
    protected Arweave(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.AR.coinCode();
    }
}
