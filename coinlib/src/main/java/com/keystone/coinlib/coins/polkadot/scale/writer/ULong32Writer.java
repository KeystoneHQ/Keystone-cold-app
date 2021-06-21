package com.keystone.coinlib.coins.polkadot.scale.writer;

import com.keystone.coinlib.coins.polkadot.scale.ScaleWriter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import java.io.IOException;

public class ULong32Writer implements ScaleWriter<Long> {
    @Override
    public void write(ScaleCodecWriter wrt, Long value) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException("Negative values are not supported: " + value);
        }
        if (value > 0xff_ff_ff_ffL) {
            throw new IllegalArgumentException("Value is too high: " + value);
        }
        wrt.directWrite((int)(value & 0xff));
        wrt.directWrite((int)((value >> 8) & 0xff));
        wrt.directWrite((int)((value >> 16) & 0xff));
        wrt.directWrite((int)((value >> 24) & 0xff));
    }
}
