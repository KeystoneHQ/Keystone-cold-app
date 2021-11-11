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

package com.keystone.coinlib.coins.BCH;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.github.kiulian.converter.AddressConverter;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Bch extends Btc {
    public Bch(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return "BCH";
    }

    public static String toCashAddress(String legacyAddress) {
        if (legacyAddress.startsWith("q") || legacyAddress.startsWith("bitcoincash")) {
            return legacyAddress;
        } else {
            try {
                return AddressConverter.toCashAddress(legacyAddress).replace("bitcoincash:","");
            }catch (Exception e) {
                return legacyAddress;
            }

        }
    }

    public static class Tx extends Btc.Tx {
        public Tx(JSONObject metaData, String coinCode)
                throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }

        @Override
        protected String convertAddress(String outAddress) {
            return Bch.toCashAddress(outAddress);
        }

        @Override
        protected void parseInput() throws JSONException, InvalidTransactionException {
            JSONArray inputs = metaData.getJSONArray("inputs");
            StringBuilder paths = new StringBuilder();
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject input = inputs.getJSONObject(i);
                String path = input.getString("ownerKeyPath");
                checkHdPath(path, false);
                paths.append(path).append(SEPARATOR);
                int index = input.optInt("index");
                if (index == 0) {
                    input.put("index", 0);
                }
                inputAmount += input.getLong("value");
            }
            hdPath = paths.deleteCharAt(paths.length() - 1).toString();
        }
    }

    public static class Deriver extends AbsDeriver {
        private static boolean legacyAddress = false;

        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(xPubKey, changeIndex, addrIndex);
            LegacyAddress addr = LegacyAddress.fromPubKeyHash(MAINNET, address.getPubKeyHash());
            String legacy = addr.toBase58();
            if (legacyAddress) return legacy;
            return Bch.toCashAddress(legacy);
        }

        @Override
        public String derive(String xPubKey) {
            String legacy = LegacyAddress.fromPubKeyHash(MAINNET,
                    getDeterministicKey(xPubKey).getPubKeyHash()).toBase58();
            if (legacyAddress) return legacy;
            return Bch.toCashAddress(legacy);
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
