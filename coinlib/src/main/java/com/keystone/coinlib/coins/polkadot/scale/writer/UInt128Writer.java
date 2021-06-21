package com.keystone.coinlib.coins.polkadot.scale.writer;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleWriter;
import com.keystone.coinlib.coins.polkadot.scale.reader.UInt128Reader;

import java.io.IOException;
import java.math.BigInteger;

public class UInt128Writer implements ScaleWriter<BigInteger> {

    @Override
    public void write(ScaleCodecWriter wrt, BigInteger value) throws IOException {
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Negative numbers are not supported by Uint128");
        }
        byte[] array = value.toByteArray();
        int pos = 0;
        // sometimes BigInteger gives an extra zero byte in the start of the array
        if (array[0] == 0) {
            pos++;
        }
        int len = array.length - pos;
        if (len > UInt128Reader.SIZE_BYTES) {
            throw new IllegalArgumentException("Value is to big for 128 bits. Has: " + len * 8 + " bits");
        }
        byte[] encoded = new byte[UInt128Reader.SIZE_BYTES];
        System.arraycopy(array, pos, encoded, encoded.length - len, len);
        UInt128Reader.reverse(encoded);
        wrt.directWrite(encoded, 0, UInt128Reader.SIZE_BYTES);
    }
}
