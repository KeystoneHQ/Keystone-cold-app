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

package com.keystone.coinlib.coins.CFX;

import android.text.TextUtils;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;


public class Cfx extends AbsCoin implements Coin {

    public static final int NETWORK_ID_MAINNET = 1029;
    public static final int NETWORK_ID_TESTNET = 1;

    public static final String NETWORK_MAIN = "cfx";
    public static final String NETWORK_TEST = "cfxtest";
    public static final String NETWORK_LOCAL_PREFIX = "net";
    public static final int HEX_BUFFER_LEN = 20;
    private static final byte VERSION_BYTE = 0x00;
    private static final byte[] CHECKSUM_TEMPLATE = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    private static final String DELIMITER = ":";
    private static final int CFX_ADDRESS_CHAR_LENGTH = 42;
    private static final int CHECKSUM_LEN = 8;
    private static final String HEX_PREFIX = "0X";
    private static final int HEX_PREFIX_LEN = 2;

    public Cfx(Coin impl) {
        super(impl);
    }

    public static String convertToBase32Address(String address) {
        return convertToBase32Address(address, NETWORK_ID_MAINNET);
    }

    public static String convertToBase32Address(String address, int netId) {
        if (address.startsWith("cfx:aa")) {
            return address;
        }
        if (address.startsWith("0x")) {
            address = address.substring(2);
        }
        byte[] hexBuf = Hex.decode(address);

        if (hexBuf == null || hexBuf.length != HEX_BUFFER_LEN) {
            throw new AddressException("hexBuf is null or length is not 20");
        }
        StringBuilder strBuilder = new StringBuilder();
        String chainPrefix = encodeNetId(netId);
        String payload = ConfluxBase32.encode(encodePayload(hexBuf));
        strBuilder.append(chainPrefix);
        strBuilder.append(DELIMITER);
        strBuilder.append(payload);
        strBuilder.append(createCheckSum(chainPrefix, payload));
        return strBuilder.toString();
    }

    public static String convertToHexAddress(String cfxAddress) throws AddressException {
        if (cfxAddress.startsWith("0x")) {
            return cfxAddress;
        }
        if (cfxAddress == null || !haveNetworkPrefix(cfxAddress)) {
            throw new AddressException("Invalid argument");
        }
        cfxAddress = cfxAddress.toLowerCase();
        String[] parts = cfxAddress.split(DELIMITER);
        if (parts.length < 2) {
            throw new AddressException("Address should have at least two part");
        }
        String network = parts[0];
        String payloadWithSum = parts[parts.length - 1];
        if (!ConfluxBase32.isValid(payloadWithSum)) {
            throw new AddressException("Input contain invalid base32 chars");
        }
        if (payloadWithSum.length() != CFX_ADDRESS_CHAR_LENGTH) {
            throw new AddressException("Address payload should have 42 chars");
        }
        String sum = payloadWithSum.substring(payloadWithSum.length() - CHECKSUM_LEN);
        String payload = payloadWithSum.substring(0, payloadWithSum.length() - CHECKSUM_LEN);
        if (!sum.equals(createCheckSum(network, payload))) {
            throw new AddressException("Invalid checksum");
        }
        byte[] raw = ConfluxBase32.decode(payload);
        String hexAddress = HEX_PREFIX + BaseEncoding.base16().encode(raw).substring(HEX_PREFIX_LEN);
        return hexAddress.toLowerCase();
    }

    private static boolean haveNetworkPrefix(String cfxAddressStr) {
        cfxAddressStr = cfxAddressStr.toLowerCase();
        return cfxAddressStr.startsWith(NETWORK_MAIN) || cfxAddressStr.startsWith(NETWORK_TEST) || cfxAddressStr.startsWith(NETWORK_LOCAL_PREFIX);
    }

    private static byte[] prefixToWords(String prefix) {
        byte[] result = prefix.getBytes();
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (result[i] & 0x1f);
        }
        return result;
    }

    private static long polyMod(byte[] data) {
        long c = 1L;
        for (byte datum : data) {
            byte c0 = (byte) (c >> 35);
            c = ((c & Long.decode("0x07ffffffff")) << 5) ^ datum;
            if ((c0 & 0x01) != 0) c ^= Long.decode("0x98f2bc8e61");
            if ((c0 & 0x02) != 0) c ^= Long.decode("0x79b76d99e2");
            if ((c0 & 0x04) != 0) c ^= Long.decode("0xf33e5fb3c4");
            if ((c0 & 0x08) != 0) c ^= Long.decode("0xae2eabe2a8");
            if ((c0 & 0x10) != 0) c ^= Long.decode("0x1e4f43e470");
        }
        return c ^ 1;
    }

    private static byte[] checksumBytes(long data) {
        return new byte[]{
                (byte) ((data >> 32) & 0xff),
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data) & 0xff),
        };
    }

    private static String createCheckSum(String chainPrefix, String payload) throws AddressException {
        byte[] prefixBuf = prefixToWords(chainPrefix);
        byte[] delimiterBuf = new byte[]{0};  // use 0
        byte[] payloadBuf = ConfluxBase32.decodeWords(payload);
        long n = polyMod(Bytes.concat(prefixBuf, delimiterBuf, payloadBuf, CHECKSUM_TEMPLATE));
        return ConfluxBase32.encode(checksumBytes(n));
    }

    private static byte[] encodePayload(byte[] addressBuf) {
        return Bytes.concat(new byte[]{VERSION_BYTE}, addressBuf);
    }

    private static String encodeNetId(int netId) throws AddressException {
        if (netId <= 0) {
            throw new AddressException("chainId should be passed as in range [1, 0xFFFFFFFF]");
        }
        switch (netId) {
            case NETWORK_ID_MAINNET:
                return NETWORK_MAIN;
            case NETWORK_ID_TESTNET:
                return NETWORK_TEST;
            default:
                return NETWORK_LOCAL_PREFIX + netId;
        }
    }

    @Override
    public String coinCode() {
        return Coins.CFX.coinCode();
    }

    public static class Tx extends AbsTx {

        private int tokenDecimal;

        public Tx(JSONObject metaData, String coinCode) throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }

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
                    contractAddress = convertToHexAddress(contractAddress);
                    if (TextUtils.isEmpty(contractAddress)) {
                        throw new InvalidTransactionException("invalid contractAddress");
                    }
                    metaData.put("contractAddress", contractAddress);
                }
                to = metaData.getString("to");
                metaData.put("to", convertToHexAddress(to));
                amount = calculateDisplayAmount();
                memo = metaData.optString("memo");
                boolean enableMemo = false;
                if (!TextUtils.isEmpty(memo) && !enableMemo) {
                    metaData.put("memo", "");
                }
                if (!metaData.has("nonce")) {
                    metaData.put("nonce", 0);
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
            BigDecimal gasLimit = new BigDecimal(metaData.getString("gas"));

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
        public static byte[] getAddress(byte[] publicKey) {
            byte[] hash = new Keccak.Digest256().digest(publicKey);
            byte[] buffer = Arrays.copyOfRange(hash, hash.length - 20, hash.length);  // right most 160 bits
            buffer[0] = (byte) (buffer[0] & (byte) 0x0f | (byte) 0x10);
            return buffer;
        }

        @Override
        public String derive(String accountXpub, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addrIndex);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(address.getPubKeyPoint(), address.isCompressed());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));
            return convertToBase32Address(s);
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
            return convertToBase32Address(s);
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
