package com.keystone.coinlib.coins.XTN_NATIVE_SEGWIT;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.Address;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;

public class XTN_NATIVE_SEGWIT extends Btc {
    public XTN_NATIVE_SEGWIT(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.BTC_TESTNET_NATIVE_SEGWIT.coinCode();
    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addressIndex) {
            DeterministicKey key = getAddrDeterministicKey(accountXpub, changeIndex, addressIndex);
            return Address.fromKey(TESTNET, key, Script.ScriptType.P2WPKH).toString();
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = DeterministicKey.deserializeB58(xPubKey, MAINNET);
            return Address.fromKey(TESTNET, key, Script.ScriptType.P2WPKH).toString();
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }

    }
}
