package com.keystone.coinlib.coins.cosmos;

import com.keystone.coinlib.utils.B58;

import org.bouncycastle.util.Arrays;

public class PublicKeyHelper {

    public static byte[] getCompressPublicKeyFromXpub(String xpub) {
        byte[] bytes = new B58().decode(xpub);
        return Arrays.copyOfRange(bytes, bytes.length - 4 - 33, bytes.length - 4);
    }

}
