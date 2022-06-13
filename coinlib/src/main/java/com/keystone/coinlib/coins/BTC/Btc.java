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

package com.keystone.coinlib.coins.BTC;

import android.util.Log;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.BCH.Bch;
import com.keystone.coinlib.coins.LTC.Ltc;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.Coins;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class Btc extends AbsCoin {
    public Btc(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.BTC.coinCode();
    }

    public static class Tx extends AbsTx implements UtxoTx {

        protected long inputAmount;
        private long outputAmount;
        private static final int DUST_AMOUNT = 546;
        private static final int OMNI_USDT_PROPERTYID = 31;
        private List<ChangeAddressInfo> changeAddressInfoList = new ArrayList<>();

        private String changeAddress;
        private long changeAmount;

        public Tx(JSONObject signTxObject, String coinCode) throws JSONException, InvalidTransactionException {
            super(signTxObject, coinCode);
        }

        @Override
        protected void checkHdPath() {
        }

        @Override
        public ChangeAddressInfo getChangeAddressInfo() {
            return changeAddressInfoList.isEmpty() ? null : changeAddressInfoList.get(0);
        }

        @Override
        public List<ChangeAddressInfo> getChangeAddressInfoList() {
            return changeAddressInfoList;
        }

        @Override
        public JSONArray getInputs() {
            try {
                return metaData.getJSONArray("inputs");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public JSONArray getOutputs() {
            if (isToken) {
                JSONArray out = new JSONArray();
                JSONObject toObj = new JSONObject();
                try {
                    toObj.put("address", to);
                    toObj.put("value", DUST_AMOUNT);
                    toObj.put("isChange", false);
                    out.put(toObj);
                    if (changeAmount > 0) {
                        JSONObject changeObj = new JSONObject();
                        changeObj.put("address", changeAddress);
                        changeObj.put("value", changeAmount);
                        changeObj.put("isChange", true);
                        out.put(changeObj);
                    }
                    return out;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    return metaData.getJSONArray("outputs");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected JSONObject extractMetaData(JSONObject signTxObject, String coinCode)
                throws JSONException {
            if (signTxObject.has("btcTx")) {
                return signTxObject.getJSONObject("btcTx");
            }
            return super.extractMetaData(signTxObject, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException, InvalidTransactionException {
            parseInput();
            parseOutPut();
            amount = calculateDisplayAmount();
            memo = metaData.optString("memo");
            fee = calculateDisplayFee();
        }

        @Override
        public double getAmount() {
            return super.getAmount();
        }

        private void parseOutPut() throws JSONException {
            changeAddressInfoList = new ArrayList<>();
            JSONArray outputs = metaData.getJSONArray("outputs");
            JSONArray outputsClone = new JSONArray(outputs.toString());
            StringBuilder destinations = new StringBuilder();
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                String address = output.getString("address");
                String newAddress = convertAddress(address);
                outputAmount += output.getLong("value");
                if (output.optBoolean("isChange") && output.has("changeAddressPath")) {
                    changeAmount += output.getLong("value");
                    changeAddressInfoList.add(new ChangeAddressInfo(
                            newAddress,
                            output.getString("changeAddressPath"),
                            output.getLong("value")));
                    outputsClone.remove(i);
                    continue;
                }

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                String amount = nf.format(satoshiToBtc(output.getLong("value")));
                outputsClone.getJSONObject(i).put("value", amount + " " + coinCode);
                destinations.append(address).append(SEPARATOR);
            }
            if (outputsClone.length() == 1) {
                to = destinations.deleteCharAt(destinations.length() - 1).toString();
            } else {
                to = outputsClone.toString();
            }
        }

        protected String convertAddress(String outAddress) {
            if (getCoinCode().equals(Coins.BCH.coinCode())) {
                outAddress = Bch.toCashAddress(outAddress);
            } else if (getCoinCode().equals(Coins.LTC.coinCode())) {
                outAddress = Ltc.convertAddress(outAddress);
            }
            return outAddress;
        }

        protected void parseInput() throws JSONException, InvalidTransactionException {
            JSONArray inputs = metaData.getJSONArray("inputs");
            StringBuilder paths = new StringBuilder();
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject input = inputs.getJSONObject(i);
                input.put("bip32Derivation", new JSONArray());
                String path = input.getString("ownerKeyPath");
                checkHdPath(path, false);
                paths.append(path).append(SEPARATOR);
                int index = input.optInt("index");
                if (index == 0) {
                    input.put("index", 0);
                }
                inputAmount += input.getJSONObject("utxo").getLong("value");
            }
            hdPath = paths.deleteCharAt(paths.length() - 1).toString();
        }

        private double calculateDisplayFee() throws InvalidTransactionException {
            if (inputAmount <= outputAmount) {
                throw new InvalidTransactionException("invalid btc tx: inputAmount must greater than outputAmount");
            }
            return satoshiToBtc(inputAmount - outputAmount);
        }

        private double calculateDisplayAmount() {
            return satoshiToBtc(outputAmount - changeAmount);
        }

        private double satoshiToBtc(long sat) {
            return sat / Math.pow(10, decimal);
        }
    }

    public static class Deriver extends AbsDeriver {

        @Override
        public String derive(String accountXpub, int changeIndex, int addressIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addressIndex);
            LegacyAddress addr = LegacyAddress.fromScriptHash(MAINNET,
                    segWitOutputScript(address.getPubKeyHash()).getPubKeyHash());
            return addr.toBase58();
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = DeterministicKey.deserializeB58(xPubKey, MAINNET);
            return LegacyAddress.fromScriptHash(MAINNET,
                    segWitOutputScript(key.getPubKeyHash()).getPubKeyHash()).toBase58();
        }

        protected Script segWitOutputScript(byte[] pubKeyHash) {
            return ScriptBuilder.createP2SHOutputScript(segWitRedeemScript(pubKeyHash));
        }

        private Script segWitRedeemScript(byte[] pubKeyHash) {
            return new ScriptBuilder().smallNum(0).data(pubKeyHash).build();
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
