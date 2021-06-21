package com.keystone.coinlib.coins.polkadot.scale.writer;

import com.keystone.coinlib.coins.polkadot.scale.ScaleWriter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import java.io.IOException;

public class UInt32Writer implements ScaleWriter<Integer> {
    @Override
    public void write(ScaleCodecWriter wrt, Integer value) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException("Negative values are not supported: " + value);
        }
        wrt.directWrite(value & 0xff);
        wrt.directWrite((value >> 8) & 0xff);
        wrt.directWrite((value >> 16) & 0xff);
        wrt.directWrite((value >> 24) & 0xff);
    }
}
