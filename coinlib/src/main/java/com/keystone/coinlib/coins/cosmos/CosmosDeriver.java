package com.keystone.coinlib.coins.cosmos;

import com.keystone.coinlib.coins.AbsDeriver;

public abstract class CosmosDeriver extends AbsDeriver {
    protected String prefix;

    @Override
    public String derive(String xPubKey, int changeIndex, int addrIndex) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String derive(String xPubKey) {
        byte[] pubKey = PublicKeyHelper.getCompressPublicKeyFromXpub(xPubKey);
        return AddressCodec.encodeGeneralAddress(prefix, pubKey);
    }

    @Override
    public String derive(String xPubKey, int index) {
        throw new RuntimeException("not implemented");
    }
}
