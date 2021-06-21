package com.keystone.coinlib.coins.polkadot.scale.writer;

import com.keystone.coinlib.coins.polkadot.scale.ScaleWriter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import java.io.IOException;

public class BoolWriter implements ScaleWriter<Boolean> {
    @Override
    public void write(ScaleCodecWriter wrt, Boolean value) throws IOException {
        if (value) {
            wrt.directWrite(1);
        } else {
            wrt.directWrite(0);
        }
    }
}
