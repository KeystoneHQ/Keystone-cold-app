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

package com.keystone.coinlib.coins.ETH;

import static com.keystone.coinlib.Util.cleanHexPrefix;
import static com.keystone.coinlib.Util.sha3String;

import android.text.TextUtils;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eth extends AbsCoin implements Coin {

    public static int CHAIN_ID = Network.Mainnet.getChainId();

    public Eth(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.ETH.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject metaData, String coinCode) throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }

        private int tokenDecimal;

        @Override
        protected void parseMetaData() throws InvalidTransactionException {
            try {
                fee = calculateDisplayFee();
                if (metaData.has("override")) {
                    JSONObject override = metaData.getJSONObject("override");
                    tokenDecimal = override.optInt("decimals", decimal);
                    isToken = true;
                    tokenName = override.optString("tokenShortName",
                            metaData.optString("tokenFullName", coinCode));
                    String contractAddress = override.optString("contractAddress");
                    if (TextUtils.isEmpty(contractAddress)) {
                        throw new InvalidTransactionException("invalid contractAddress");
                    }
                    metaData.put("contractAddress", contractAddress);
                }
                to = metaData.getString("to");
                amount = calculateDisplayAmount();
                memo = metaData.optString("memo");
                boolean enableMemo = false;
                int nonce = metaData.optInt("nonce", -1);
                if (nonce == -1) {
                    metaData.put("nonce", 0);
                }
                if (!TextUtils.isEmpty(memo) && !enableMemo) {
                    metaData.put("memo", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public double getAmount() {
            if (isToken) {
                return getAmountWithoutFee();
            } else {
                return super.getAmount();
            }
        }

        private double calculateDisplayFee() throws JSONException {
            BigDecimal gasPrice = new BigDecimal(metaData.getString("gasPrice"));
            BigDecimal gasLimit = new BigDecimal(metaData.getString("gasLimit"));

            return gasLimit.multiply(gasPrice)
                    .divide(BigDecimal.TEN.pow(decimal), Math.min(decimal, 8), BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        private double calculateDisplayAmount() throws JSONException {

            int decimal = isToken ? tokenDecimal : this.decimal;
            String amount = metaData.getString("value");
            BigDecimal value = new BigDecimal(amount);
            return value.divide(BigDecimal.TEN.pow(decimal), Math.min(decimal, 8), BigDecimal.ROUND_HALF_UP).doubleValue();
        }

    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addrIndex);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(address.getPubKeyPoint(), address.isCompressed());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));

            //checksum
            return toChecksumAddress(s);
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = getDeterministicKey(xPubKey);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(key.getPubKey());

            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));

            //checksum
            return toChecksumAddress(s);
        }

        @Override
        public String derive(String xPubKey, int index) {
            DeterministicKey address = deriveChild(xPubKey, index);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(address.getPubKeyPoint(), address.isCompressed());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));

            //checksum
            return toChecksumAddress(s);
        }

        public static byte[] getAddress(byte[] publicKey) {
            byte[] hash = new Keccak.Digest256().digest(publicKey);
            return Arrays.copyOfRange(hash, hash.length - 20, hash.length);  // right most 160 bits
        }

        public static String toChecksumAddress(String address) {
            if (hasUpperCase(address)) {
                return address;
            }
            String lowercaseAddress = cleanHexPrefix(address).toLowerCase();
            String addressHash = cleanHexPrefix(sha3String(lowercaseAddress));

            StringBuilder result = new StringBuilder(lowercaseAddress.length() + 2);

            result.append("0x");

            for (int i = 0; i < lowercaseAddress.length(); i++) {
                if (Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) >= 8) {
                    result.append(String.valueOf(lowercaseAddress.charAt(i)).toUpperCase());
                } else {
                    result.append(lowercaseAddress.charAt(i));
                }
            }
            return result.toString();
        }

        public static boolean hasUpperCase(String address) {
            String regex = ".*[A-F]+.*";
            Matcher matcher = Pattern.compile(regex).matcher(address);
            return matcher.matches();
        }
    }
}
