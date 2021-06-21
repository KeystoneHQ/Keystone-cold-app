package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;

import java.math.BigInteger;

public class UInt128Reader implements ScaleReader<BigInteger> {

    public static final int SIZE_BYTES = 16;

    public static void reverse(byte[] value) {
        for (int i = 0; i < value.length / 2; i++) {
            int other = value.length - i - 1;
            byte tmp = value[other];
            value[other] = value[i];
            value[i] = tmp;
        }
    }

    @Override
    public BigInteger read(ScaleCodecReader rdr) {
        byte[] value = rdr.readByteArray(SIZE_BYTES);
        reverse(value);
        return new BigInteger(1, value);
    }
}
