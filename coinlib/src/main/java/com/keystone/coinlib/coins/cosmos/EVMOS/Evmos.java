package com.keystone.coinlib.coins.cosmos.EVMOS;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.cosmos.AddressCodec;
import com.keystone.coinlib.coins.cosmos.CosmosDeriver;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;

public class Evmos extends AbsCoin {

    protected Evmos(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.EVMOS.coinCode();
    }


    public static class Deriver extends CosmosDeriver {

        public Deriver(){
            prefix = "evmos";
        }


        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = getDeterministicKey(xPubKey);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(key.getPubKey());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);
            return AddressCodec.encodeEvmosAddress(prefix, hash);
        }
    }
}
