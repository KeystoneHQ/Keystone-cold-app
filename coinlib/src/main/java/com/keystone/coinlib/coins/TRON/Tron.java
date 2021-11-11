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

package com.keystone.coinlib.coins.TRON;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import static com.keystone.coinlib.coins.ETH.Eth.Deriver.getAddress;

public class Tron extends AbsCoin {
    public Tron(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.TRON.coinCode();
    }

    public static class Tx extends AbsTx {

        private int tokenDecimals;

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            from = metaData.getString("from");
            to = metaData.getString("to");
            fee = metaData.getInt("fee") / Math.pow(10, decimal);
            memo = metaData.optString("memo");
            if ((metaData.has("token") || metaData.has("contractAddress"))
                    && metaData.has("override")) {
                isToken = true;
                JSONObject override = metaData.getJSONObject("override");
                tokenName = override.optString("tokenShortName",
                        metaData.optString("tokenFullName", coinCode));
                tokenDecimals = override.optInt("decimals");
            }

            amount = calculateDisplayAmount();
        }

        private double calculateDisplayAmount() throws JSONException {
            int decimal = isToken ? tokenDecimals : this.decimal;
            String amount = metaData.getString("value");
            BigDecimal value = new BigDecimal(amount);
            return value.divide(BigDecimal.TEN.pow(decimal), Math.min(decimal, 8), BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        @Override
        public double getAmount() {
            if (isToken) {
                return getAmountWithoutFee();
            } else {
                return super.getAmount();
            }
        }
    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addrIndex) {

            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addrIndex);

            ECKey eckey = ECKey.fromPublicOnly(address.getPubKeyPoint(), address.isCompressed());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            byte[] addr = getAddress(hash);

            return Base58.encodeChecked(0x41, addr);
        }

        @Override
        public String derive(String xPubKey) {
            ECKey eckey = ECKey.fromPublicOnly(getDeterministicKey(xPubKey).getPubKeyPoint(), getDeterministicKey(xPubKey).isCompressed());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            byte[] addr = getAddress(hash);

            return Base58.encodeChecked(0x41, addr);
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
