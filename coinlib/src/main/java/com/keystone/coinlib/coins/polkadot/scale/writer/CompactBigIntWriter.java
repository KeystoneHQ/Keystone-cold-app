package com.keystone.coinlib.coins.polkadot.scale.writer;

import com.keystone.coinlib.coins.polkadot.scale.CompactMode;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public class CompactBigIntWriter implements ScaleWriter<BigInteger> {

    private static final CompactULongWriter LONG_WRITER = new CompactULongWriter();

    @Override
    public void write(ScaleCodecWriter wrt, BigInteger value) throws IOException {
        CompactMode mode = CompactMode.forNumber(value);

        byte[] data = value.toByteArray();
        int noneZeroPos = 0;
        while (noneZeroPos < data.length - 1) {
            if (data[noneZeroPos] == 0) {
                noneZeroPos++;
            } else {
                break;
            }
        }
        data = Arrays.copyOfRange(data, noneZeroPos, data.length);
        int pos = data.length-1;
        int limit = 0;

        if (mode != CompactMode.BIGINT) {
            LONG_WRITER.write(wrt, value.longValue());
            return;
        }

        wrt.directWrite(((data.length - 4) << 2) + mode.getValue());
        while (pos >= 0) {
            wrt.directWrite(data[pos]);
            pos--;
        }
    }
}
