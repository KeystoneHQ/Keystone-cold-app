package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class UInt32Reader implements ScaleReader<Long> {
    @Override
    public Long read(ScaleCodecReader rdr) {
        long result = 0;
        result += (long)rdr.readUByte();
        result += ((long)rdr.readUByte()) << 8;
        result += ((long)rdr.readUByte()) << (2 * 8);
        result += ((long)rdr.readUByte()) << (3 * 8);
        return result;
    }

    public Long readBE(ScaleCodecReader rdr) {
        long result = 0;
        result += ((long)rdr.readUByte()) << (3 * 8);
        result += ((long)rdr.readUByte()) << (2 * 8);
        result += ((long)rdr.readUByte()) << 8;
        result += rdr.readUByte();
        return result;
    }
}
