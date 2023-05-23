package com.keystone.coinlib.coins.SUI;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;

public class Sui extends AbsCoin {
    protected Sui(Coin impl) {
        super(impl);
    }

    public enum IntentScope {
        TransactionData(0),
        PersonalMessage(3);

        private final int value;

        IntentScope(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static IntentScope fromValue(Integer value) {
            for(IntentScope scope : IntentScope.values()) {
                if(scope.getValue() == value) {
                    return scope;
                }
            }

            throw new IllegalArgumentException("Unknown sui intent scope: " + value);
        }
    }

    @Override
    public String coinCode() {
        return Coins.SUI.coinCode();
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
            pubKeyWithSchema[0] = 0x00;
            System.arraycopy(pubKey, 0, pubKeyWithSchema, 1, pubKey.length);
            String address = "0x" + Hex.toHexString(Util.blake2b256(pubKeyWithSchema));
            return address;
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
