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

package com.keystone.coinlib.coins.LTC;

import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Ltc extends Btc {
    public Ltc(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.LTC.coinCode();
    }

    public static class Tx extends Btc.Tx {

        public Tx(JSONObject metaData, String coinCode) throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }

        @Override
        protected String convertAddress(String outAddress) {
            return Ltc.convertAddress(outAddress);
        }
    }

    public static String convertAddress(String address) {
        if (address.startsWith("M")) {
            return address;
        }
        try {
            byte[] data = Base58.decode(address);
            return Base58.encodeChecked(0x32, Arrays.copyOfRange(data,1, data.length - 4));
        } catch (Exception e) {
            return address;
        }

    }

    public static class Deriver extends Btc.Deriver {

        static final boolean legacyAddress = false;

        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {

            if (legacyAddress) {
                return super.derive(xPubKey, changeIndex, addrIndex);
            }
            DeterministicKey address = getAddrDeterministicKey(xPubKey, changeIndex, addrIndex);

            LegacyAddress addr = LegacyAddress.fromScriptHash(MAINNET,
                    segWitOutputScript(address.getPubKeyHash()).getPubKeyHash());
            return Base58.encodeChecked(0x32, addr.getHash());
        }

        @Override
        public String derive(String xPubKey) {
            if (legacyAddress) {
                return super.derive(xPubKey);
            }
            LegacyAddress addr = LegacyAddress.fromScriptHash(MAINNET,
                    segWitOutputScript(getDeterministicKey(xPubKey).getPubKeyHash()).getPubKeyHash());
            return Base58.encodeChecked(0x32, addr.getHash());
        }
    }
}
