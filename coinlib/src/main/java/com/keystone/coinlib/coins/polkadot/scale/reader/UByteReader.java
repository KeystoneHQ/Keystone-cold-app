package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class UByteReader implements ScaleReader<Integer> {
    @Override
    public Integer read(ScaleCodecReader rdr) {
        byte x = rdr.readByte();
        if (x < 0) {
            return 256 + (int)x;
        }
        return (int)x;
    }
}
