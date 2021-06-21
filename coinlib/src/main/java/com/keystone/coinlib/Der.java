package com.keystone.coinlib;

import android.util.Log;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

public class Der {

    private Der() {
        throw new UnsupportedOperationException("Der is a utility class and cannot be instantiated");
    }

    public static byte[] toDer(byte[] rs) {
        byte[] r = Arrays.copyOfRange(rs, 0,32);
        byte[] s = Arrays.copyOfRange(rs, 32,64);
        return Der.encodeSequence(Der.encodeBytes(r), Der.encodeBytes(s)).getBytes();
    }

    /**
     * @param encodedPieces
     * @return
     */
    public static ByteString encodeSequence(ByteString... encodedPieces) {
        int totalLen = 0;
        ByteString stringPieces = new ByteString(toBytes(0x30));
        for (ByteString p : encodedPieces) {
            totalLen += p.length();
            stringPieces.insert(p.getBytes());
        }
        stringPieces.insert(1, encodeLength(totalLen).getBytes());
        return stringPieces;
    }

    /**
     * @param length
     * @return
     */
    public static ByteString encodeLength(int length) {
        assert length >= 0;
        if (length < 0x80) {
            return new ByteString(toBytes(length));
        }
        String hexString = String.format("%x", length);
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        ByteString s = new ByteString(Hex.decode(hexString));
        s.insert(0, toBytes((0x80 | s.length())));
        return s;

    }


    public static ByteString encodeBytes(byte[] bytes) {
        ByteString s = new ByteString(bytes);
        short num = s.getShort(0);
        if (num <= 0x7F) {
            s.insert(0, toBytes(s.length()));
            s.insert(0, toBytes(0x02));
            return s;
        }
        int length = s.length();
        s.insert(0, toBytes(0x00));
        s.insert(0, toBytes((length + 1)));
        s.insert(0, toBytes(0x02));
        return s;
    }



    public static byte[] toBytes(int c) {
        return new byte[]{(byte) c};
    }


}

