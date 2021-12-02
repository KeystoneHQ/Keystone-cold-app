package com.keystone.coinlib.coins.XTN_SEGWIT;

import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.DeterministicKey;

public class XTN_SEGWIT extends Btc {
    public XTN_SEGWIT(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.BTC_TESTNET_SEGWIT.coinCode();
    }

    public static class Deriver extends Btc.Deriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addressIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addressIndex);
            return LegacyAddress.fromScriptHash(TESTNET,
                    super.segWitOutputScript(address.getPubKeyHash()).getPubKeyHash()).toBase58();
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = DeterministicKey.deserializeB58(xPubKey, MAINNET);
            return LegacyAddress.fromScriptHash(TESTNET,
                    segWitOutputScript(key.getPubKeyHash()).getPubKeyHash()).toBase58();
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }

    }

}
