package com.keystone.coinlib.coins.BTC_LEGACY;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.DeterministicKey;

public class BTC_LEGACY extends Btc {
    public BTC_LEGACY(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.BTC_LEGACY.coinCode();
    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addressIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addressIndex);
            return LegacyAddress.fromPubKeyHash(MAINNET, address.getPubKeyHash()).toBase58();
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = DeterministicKey.deserializeB58(xPubKey, MAINNET);
            return LegacyAddress.fromPubKeyHash(MAINNET, key.getPubKeyHash()).toBase58();
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }

    }
}
