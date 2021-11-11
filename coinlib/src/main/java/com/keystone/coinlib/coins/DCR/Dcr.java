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

package com.keystone.coinlib.coins.DCR;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.cryptohash.BLAKE256;
import fr.cryptohash.Digest;
import fr.cryptohash.RIPEMD160;


public class Dcr extends AbsCoin implements Coin {
    public Dcr(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.DCR.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            parseInput();
            to = metaData.getString("to");
            amount = metaData.getLong("amount") / Math.pow(10, decimal);
            memo = metaData.optString("memo");
            fee = metaData.getLong("fee") / Math.pow(10, decimal);
        }

        private void parseInput() throws JSONException {
            JSONArray inputs = metaData.getJSONArray("inputs");
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject input = inputs.getJSONObject(i);
                int index = input.optInt("outputIndex");
                if (index == 0) {
                    input.put("outputIndex", 0);
                }
            }
        }
    }

    public static class Deriver extends AbsDeriver {

        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(xPubKey, changeIndex, addrIndex);
            byte[] hash = blake256Ripemd160(address.getPubKey());
            return encodeChecked(hash);
        }

        @Override
        public String derive(String xPubKey) {
            return encodeChecked(blake256Ripemd160(getDeterministicKey(xPubKey).getPubKey()));
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }

        private byte[] blake256Ripemd160(byte[] publicKey) {
            Digest digestBlake256 = new BLAKE256().copy();
            digestBlake256.update(publicKey);
            byte[] hash1 = digestBlake256.digest();

            Digest digest160 = new RIPEMD160().copy();
            digest160.update(hash1);
            return digest160.digest();
        }

        private String encodeChecked(byte[] payload) {
            // A stringified buffer is:
            // 2 byte version + data bytes + 4 bytes check code (a truncated hash)

            byte[] versionBytes = new byte[]{0x07, 0x3f};
            byte[] addressBytes = new byte[2 + payload.length + 4];
            addressBytes[0] = versionBytes[0];
            addressBytes[1] = versionBytes[1];
            System.arraycopy(payload, 0, addressBytes, 2, payload.length);
            byte[] checksum = doubleBlake256(addressBytes, 0, payload.length + 2);
            System.arraycopy(checksum, 0, addressBytes, payload.length + 2, 4);
            return org.bitcoinj.core.Base58.encode(addressBytes);
        }

        private byte[] doubleBlake256(byte[] data, int offset, int length) {
            Digest digest = new BLAKE256().copy();
            digest.update(data, offset, length);
            return digest.digest(digest.digest());
        }


    }
}
