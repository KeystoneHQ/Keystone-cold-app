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

import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.coins.BTC_LEGACY.BTC_LEGACY;
import com.keystone.coinlib.coins.BTC_NATIVE_SEGWIT.BTC_NATIVE_SEGWIT;
import com.keystone.coinlib.coins.XTN_LEGACY.XTN_LEGACY;
import com.keystone.coinlib.coins.XTN_NATIVE_SEGWIT.XTN_NATIVE_SEGWIT;
import com.keystone.coinlib.coins.XTN_SEGWIT.XTN_SEGWIT;
import com.keystone.coinlib.coins.polkadot.DOT.Dot;
import com.keystone.coinlib.coins.polkadot.KSM.Ksm;
import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.path.Account;
import com.keystone.coinlib.path.AddressIndex;
import com.keystone.coinlib.path.Change;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.path.CoinType;
import com.keystone.coinlib.utils.Arith;
import com.keystone.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public abstract class AbsTx {

    protected String txId;
    protected String from;
    protected String to;
    protected double amount;
    protected double fee;
    protected String memo;
    protected int decimal;
    protected JSONObject metaData;
    protected final String coinCode;
    protected String hdPath;
    protected String txType;
    protected String tokenName;
    protected boolean isToken;

    public static final String SEPARATOR = ",";

    public AbsTx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
        this.coinCode = coinCode;
        this.decimal = object.getInt("decimal");

        if (coinCode.equals(Coins.XTN.coinCode())) {
           this.metaData =  extractMetaData(object, Coins.BTC.coinCode());
        } else {
            this.metaData = extractMetaData(object, coinCode);
        }

        this.hdPath = object.optString("hdPath");
        checkHdPath();
        this.txType = coinCode;
        if (metaData == null) {
            throw new InvalidTransactionException("invalid sign tx metaData");
        }
        parseMetaData();
    }

    protected void checkHdPath() throws InvalidTransactionException {
        checkHdPath(hdPath, false);
    }

    protected void checkHdPath(String hdPath, boolean allHardend) throws InvalidTransactionException {
        try {
            AddressIndex address = CoinPath.parsePath(hdPath, allHardend);
            Change change = address.getParent();

            Account account = change.getParent();
            if (account.getValue() != 0) {
                throw new InvalidTransactionException("invalid hdPath, error account value");
            }

            CoinType coinType = account.getParent();
            if ((coinType.getValue()!=0 && coinType.getValue() != 1) && !coinCode.equals(Coins.coinCodeOfIndex(coinType.getValue()))) {
                throw new InvalidTransactionException("invalid hdPath, error coinIndex");
            }

            if(coinType.getValue() == 0 && !Coins.isBTCMainnet(coinCode)) {
                throw new InvalidTransactionException("invalid hdPath, error coinIndex");
            }

            if(coinType.getValue() == 1 && !Coins.isBTCTestnet(coinCode)) {
                throw new InvalidTransactionException("invalid hdPath, error coinIndex");
            }

            if (Coins.purposeNumber(coinCode) != coinType.getParent().getValue()) {
                throw new InvalidTransactionException("invalid hdPath, error purpose number");
            }
        } catch (InvalidPathException e) {
            e.printStackTrace();
            throw new InvalidTransactionException("invalid hdPath");
        }
    }

    public static AbsTx newInstance(JSONObject object) throws JSONException {

        String coinCode = object.getString("coinCode");

        try {
            if (coinCode.equals(Coins.DOT.coinCode())) return new Dot.Tx(object, coinCode);
            else if (coinCode.equals(Coins.KSM.coinCode())) return new Ksm.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC_LEGACY.coinCode())) return new Btc.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) return new Btc.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC.coinCode())) return new Btc.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC_TESTNET_LEGACY.coinCode())) return new Btc.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC_TESTNET_NATIVE_SEGWIT.coinCode())) return new Btc.Tx(object, coinCode);
            else if(coinCode.equals(Coins.BTC_TESTNET_SEGWIT.coinCode())) return new Btc.Tx(object, coinCode);
            Class<?> clazz = Class.forName(CoinReflect.getCoinClassByCoinCode(coinCode) + "$Tx");
            return (AbsTx) clazz.getDeclaredConstructor(JSONObject.class, String.class)
                    .newInstance(object, coinCode);
        } catch (ClassNotFoundException
                | InstantiationException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException
                | InvalidTransactionException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getTxType() {
        return txType;
    }

    public String getTxId() {
        return txId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return Arith.add(amount, fee);
    }

    protected double getAmountWithoutFee() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public JSONObject getMetaData() {
        return metaData;
    }

    protected abstract void parseMetaData() throws JSONException, InvalidTransactionException;

    protected JSONObject extractMetaData(JSONObject signTxObject, String coinCode) throws JSONException {
        return signTxObject.getJSONObject(coinCode.toLowerCase() + "Tx");
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public String getHdPath() {
        return hdPath;
    }

    public String getUnit() {
        if (isToken) {
            return tokenName;
        } else {
            if(coinCode.startsWith("BTC")) {
                return "BTC";
            }
            return coinCode;
        }
    }
}
