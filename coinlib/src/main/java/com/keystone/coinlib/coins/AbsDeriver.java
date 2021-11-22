/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.coins;

import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.BTC_P2PKH.BTC_P2PKH;
import com.keystone.coinlib.coins.BTC_P2WPKH.BTC_P2WPKH;
import com.keystone.coinlib.coins.polkadot.DOT.Dot;
import com.keystone.coinlib.coins.polkadot.KSM.Ksm;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;

public abstract class AbsDeriver {
    public static AbsDeriver newInstance(@NonNull String coinCode) {
        try {
            if (coinCode.equals(Coins.DOT.coinCode())) return new Dot.Deriver();
            else if (coinCode.equals(Coins.KSM.coinCode())) return new Ksm.Deriver();
            else if(coinCode.equals(Coins.BTC_P2PKH.coinCode())) return new BTC_P2PKH.Deriver();
            else if(coinCode.equals(Coins.BTC_P2WPKH.coinCode())) return new BTC_P2WPKH.Deriver();
            else {
                Class clazz = Class.forName(CoinReflect.getCoinClassByCoinCode(coinCode) + "$Deriver");
                return (AbsDeriver) clazz.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static final NetworkParameters MAINNET = MainNetParams.get();

    protected static final NetworkParameters TESTNET = RegTestParams.get();

    protected DeterministicKey getAddrDeterministicKey(String accountXpub, int changeIndex, int addressIndex) {
        DeterministicKey account = DeterministicKey.deserializeB58(accountXpub, MAINNET);
        DeterministicKey change = HDKeyDerivation.deriveChildKey(account, changeIndex);
        return HDKeyDerivation.deriveChildKey(change, addressIndex);
    }

    protected DeterministicKey deriveChild(String accountXpub, int index) {
        DeterministicKey account = DeterministicKey.deserializeB58(accountXpub, MAINNET);
        return HDKeyDerivation.deriveChildKey(account, index);
    }

    protected DeterministicKey getDeterministicKey(String xPub) {
        return DeterministicKey.deserializeB58(xPub, MAINNET);
    }

    public abstract String derive(String xPubKey, int changeIndex, int addrIndex);

    public abstract String derive(String xPubKey);

    public abstract String derive(String xPubKey, int index);
}
