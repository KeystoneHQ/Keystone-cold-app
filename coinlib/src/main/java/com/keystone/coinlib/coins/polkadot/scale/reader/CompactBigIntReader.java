package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.CompactMode;
import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

import java.math.BigInteger;

public class CompactBigIntReader implements ScaleReader<BigInteger> {

    private static final CompactUIntReader intReader = new CompactUIntReader();

    @Override
    public BigInteger read(ScaleCodecReader rdr) {
        int type = rdr.readUByte();
        CompactMode mode = CompactMode.byValue((byte)(type & 0b11));
        if (mode != CompactMode.BIGINT) {
            rdr.skip(-1);
            int value = intReader.read(rdr);
            return BigInteger.valueOf(value);
        }
        int len = (type >> 2) + 4;
        byte[] value = rdr.readByteArray(len);
        //LE encoded, so need to reverse it
        for (int i = 0; i < value.length / 2; i++) {
            byte temp = value[i];
            value[i] = value[value.length -i -1];
            value[value.length - i - 1] = temp;
        }
        //unsigned, i.e. always positive, signum=1
        return new BigInteger(1, value);
    }
}
