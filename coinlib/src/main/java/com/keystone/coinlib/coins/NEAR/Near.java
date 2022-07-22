package com.keystone.coinlib.coins.NEAR;

import android.util.Log;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;

public class Near extends AbsCoin {


    protected Near(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.NEAR.coinCode();
    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = org.bouncycastle.util.Arrays.copyOfRange(bytes, bytes.length - 4 - 32, bytes.length - 4);
            return Hex.toHexString(pubKey);
        }

        @Override
        public String derive(String xPubKey) {

            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = org.bouncycastle.util.Arrays.copyOfRange(bytes, bytes.length - 4 - 32, bytes.length - 4);
            return Hex.toHexString(pubKey);
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
