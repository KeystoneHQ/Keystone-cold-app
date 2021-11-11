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

package com.keystone.coinlib.coins.EOS;

import android.text.TextUtils;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;

import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.DeterministicKey;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.json.JSONException;
import org.json.JSONObject;

import static com.keystone.coinlib.Util.concat;

public class Eos extends AbsCoin {
    public Eos(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return "EOS";
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            JSONObject data = metaData.getJSONObject("data");
            to = data.getString("to");
            from = data.getString("from");
            memo = data.optString("memo");
            int decimal = data.optInt("decimal");
            if (decimal == 0) {
                data.put("decimal", this.decimal);
                decimal = this.decimal;
            }
            amount = (double) data.getLong("amount") / Math.pow(10, decimal);
            fee = (double) data.optLong("fee") / Math.pow(10, decimal);
            String symbol = data.optString("symbol");
            if (!TextUtils.isEmpty(symbol) && !"EOS".equals(symbol)) {
                isToken = true;
                tokenName = symbol;
            }
        }
    }

    public static class Deriver extends AbsDeriver {

        @Override
        public String derive(String accountXpub, int changeIndex, int addressIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, 0, 0);

            return getEosPubKey(address);
        }

        @Override
        public String derive(String xPubKey) {
            return getEosPubKey(getDeterministicKey(xPubKey));
        }

        private String getEosPubKey(DeterministicKey address) {
            byte[] pubkeyByte = address.getPubKey();
            byte[] csum = ripeMD160Hash(pubkeyByte);
            csum = copy(csum, 0, 4);

            byte[] addy = concat(pubkeyByte, csum);
            return "EOS" + Base58.encode(addy);
        }

        static byte[] ripeMD160Hash(byte[] pubKey) {
            RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
            ripemd160.update(pubKey, 0, pubKey.length);
            byte[] output = new byte[ripemd160.getDigestSize()];
            ripemd160.doFinal(output, 0);
            return output;
        }

        public static byte[] copy(byte[] src, int start, int length) {
            byte[] c = new byte[length];
            System.arraycopy(src, start, c, 0, length);
            return c;
        }


        public static byte[] copy(byte[] src, int start, byte[] dest, int dstart, int length) {
            System.arraycopy(src, start, dest, dstart, length);
            return dest;
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
