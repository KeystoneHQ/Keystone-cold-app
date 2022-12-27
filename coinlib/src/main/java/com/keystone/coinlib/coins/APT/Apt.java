package com.keystone.coinlib.coins.APT;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;


public class Apt extends AbsCoin {

    protected Apt(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.APTOS.coinCode();
    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public String derive(String xPubKey) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = org.bouncycastle.util.Arrays.copyOfRange(bytes, bytes.length - 4 - 32, bytes.length - 4);
            byte[] pubKeyWithSchema = new byte[pubKey.length + 1];
            System.arraycopy(pubKey, 0, pubKeyWithSchema, 0, pubKey.length);
            pubKeyWithSchema[pubKeyWithSchema.length - 1] = 0x00;
            String address = "0x" + Hex.toHexString(Util.sha3256(pubKeyWithSchema));
            return address;
        }


        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
