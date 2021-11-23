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

package com.keystone.cold.viewmodel.tx;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.model.Tx;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class GenericETHTxEntity implements Tx {
    // GenericTx params
    private String txId;
    private String amount;
    private BigDecimal amountValue;
    private String from;
    private String to;
    private String fee;
    private BigDecimal feeValue;
    private String signedHex;
    private long timeStamp;
    private String memo;
    private String belongTo;

    // feeMarket params
    private String estimatedFee;
    private String maxFee;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String gasLimit;

    private BigDecimal estimatedFeeValue;
    private BigDecimal maxFeeValue;
    private BigDecimal maxFeePerGasValue;
    private BigDecimal maxPriorityFeePerGasValue;
    private BigDecimal gasLimitValue;

    // another params
    private String signature;
    private int chainId;
    private String addition;
    private int txType; // 0x00:legacy  0x02:feeMarket
    private boolean isFromTFCard;

    public static GenericETHTxEntity transformDbEntity(Web3TxEntity web3TxEntity) {
        GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
        try {
            JSONObject ethTx;
            int txType = web3TxEntity.getTxType();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(20);
            BigDecimal gasLimit, amount, value;
            switch (txType) {
                case 0x00:
                    ethTx = EthImpl.decodeTransaction(web3TxEntity.getSignedHex(), null);
                    if (ethTx == null) {
                        return null;
                    }
                    genericETHTxEntity = getGenericETHTxEntity(ethTx, web3TxEntity);
                    genericETHTxEntity.setSignature(EthImpl.getSignature(web3TxEntity.getSignedHex()));
                    int chainId = getChainIdByEIP155(ethTx.getInt("chainId"));
                    genericETHTxEntity.setChainId(chainId);
                    BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
                    gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
                    genericETHTxEntity.setGasLimit(nf.format(gasLimit));
                    genericETHTxEntity.setGasLimitValue(gasLimit);
                    BigDecimal fee = gasLimit.multiply(gasPrice).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    amount = new BigDecimal(ethTx.getString("value"));
                    value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    genericETHTxEntity.setAmountValue(amount);
                    genericETHTxEntity.setAmount(nf.format(value) + Web3TxViewModel.getSymbol(chainId));
                    genericETHTxEntity.setFee(nf.format(fee) + Web3TxViewModel.getSymbol(chainId));
                    genericETHTxEntity.setFeeValue(fee);
                    JSONObject addition = new JSONObject(web3TxEntity.getAddition());
                    genericETHTxEntity.setFromTFCard(addition.getBoolean("isFromTFCard"));
                    break;
                case 0x02:
                    ethTx = EthImpl.decodeEIP1559Transaction(web3TxEntity.getSignedHex(), null);
                    if (ethTx == null) {
                        return null;
                    }
                    genericETHTxEntity = getGenericETHTxEntity(ethTx, web3TxEntity);
                    genericETHTxEntity.setSignature(EthImpl.getEIP1559Signature(web3TxEntity.getSignedHex()));
                    genericETHTxEntity.setChainId(ethTx.getInt("chainId"));
                    BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
                    BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
                    gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
                    BigDecimal estimatedFee = gasPriorityPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    BigDecimal maxFee = gasLimitPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    genericETHTxEntity.setMaxFeeValue(maxFee);
                    genericETHTxEntity.setMaxPriorityFeePerGasValue(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
                    genericETHTxEntity.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
                    genericETHTxEntity.setMaxFeePerGasValue(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
                    genericETHTxEntity.setMaxFeePerGas(nf.format(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
                    genericETHTxEntity.setGasLimit(nf.format(gasLimit));
                    genericETHTxEntity.setEstimatedFeeValue(estimatedFee);
                    genericETHTxEntity.setEstimatedFee(nf.format(estimatedFee) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    genericETHTxEntity.setMaxFee(nf.format(maxFee) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    amount = new BigDecimal(ethTx.getString("value"));
                    value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    genericETHTxEntity.setAmount(nf.format(value) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    JSONObject additionJson = new JSONObject(web3TxEntity.getAddition());
                    genericETHTxEntity.setFromTFCard(additionJson.getBoolean("isFromTFCard"));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return genericETHTxEntity;
    }

    public static Web3TxEntity transToDbEntity(GenericETHTxEntity genericETHTxEntity) {
        Web3TxEntity web3TxEntity = new Web3TxEntity();
        web3TxEntity.setTxId(genericETHTxEntity.getTxId());
        web3TxEntity.setSignedHex(genericETHTxEntity.getSignedHex());
        web3TxEntity.setFrom(genericETHTxEntity.getFrom());
        web3TxEntity.setTimeStamp(genericETHTxEntity.getTimeStamp());
        web3TxEntity.setBelongTo(genericETHTxEntity.getBelongTo());
        web3TxEntity.setTxType(genericETHTxEntity.getTxType());
        web3TxEntity.setAddition(genericETHTxEntity.getAddition());
        return web3TxEntity;
    }

    private static GenericETHTxEntity getGenericETHTxEntity(JSONObject ethTx, Web3TxEntity web3TxEntity) throws JSONException {
        GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
        genericETHTxEntity.setTxId(web3TxEntity.getTxId());
        genericETHTxEntity.setSignedHex(web3TxEntity.getSignedHex());
        genericETHTxEntity.setFrom(web3TxEntity.getFrom());
        genericETHTxEntity.setTimeStamp(web3TxEntity.getTimeStamp());
        genericETHTxEntity.setBelongTo(web3TxEntity.getBelongTo());
        genericETHTxEntity.setTxType(web3TxEntity.getTxType());
        genericETHTxEntity.setAddition(web3TxEntity.getAddition());
        genericETHTxEntity.setMemo(ethTx.getString("data"));
        genericETHTxEntity.setTo(ethTx.getString("to"));
        genericETHTxEntity.setMemo(ethTx.getString("data"));
        String currentBelongTo = Utilities.getCurrentBelongTo(MainApplication.getApplication());
        genericETHTxEntity.setBelongTo(currentBelongTo);
        return genericETHTxEntity;
    }

    private static int getChainIdByEIP155(int chainId) {
        return (chainId - 8 - 27) / 2;
    }

    @NonNull
    @Override
    public String getTxId() {
        return txId;
    }

    @Override
    public String getCoinId() {
        return Coins.ETH.coinId();
    }

    @Override
    public String getCoinCode() {
        return Coins.ETH.coinCode();
    }

    @Override
    public String getDisplayName() {
        return Coins.ETH.coinCode();
    }

    @Override
    public String getAmount() {
        return amount;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public String getFee() {
        return fee;
    }

    @Override
    public String getSignedHex() {
        return signedHex;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String getMemo() {
        return memo;
    }

    @Override
    public String getSignId() {
        return WatchWallet.METAMASK_SIGN_ID;
    }

    @Override
    public String getBelongTo() {
        return belongTo;
    }

    public String getEstimatedFee() {
        return estimatedFee;
    }

    public String getMaxFee() {
        return maxFee;
    }

    public String getMaxFeePerGas() {
        return maxFeePerGas;
    }

    public String getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getSignature() {
        return signature;
    }

    public int getChainId() {
        return chainId;
    }

    public String getAddition() {
        return addition;
    }

    public int getTxType() {
        return txType;
    }

    public boolean isFromTFCard() {
        return isFromTFCard;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public void setSignedHex(String signedHex) {
        this.signedHex = signedHex;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public void setEstimatedFee(String estimatedFee) {
        this.estimatedFee = estimatedFee;
    }

    public void setMaxFee(String maxFee) {
        this.maxFee = maxFee;
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public void setFromTFCard(boolean fromTFCard) {
        isFromTFCard = fromTFCard;
    }

    @Override
    public boolean filter(String s) {

        if (TextUtils.isEmpty(s)) {
            return true;
        }
        s = s.toLowerCase();
        return from.toLowerCase().contains(s)
                || to.toLowerCase().contains(s)
                || txId.toLowerCase().contains(s)
                || memo.toLowerCase().contains(s);
    }

    @NonNull
    @Override
    public String toString() {
        return "TxEntity{" +
                "txId='" + getTxId() + '\'' +
                ", coinId='" + getCoinId() + '\'' +
                ", coinCode='" + getCoinCode() + '\'' +
                ", amount='" + getAmount() + '\'' +
                ", from='" + getFrom() + '\'' +
                ", to='" + getTo() + '\'' +
                ", estimatedFee='" + getEstimatedFee() + '\'' +
                ", maxFee='" + getMaxFee() + '\'' +
                ", maxFeePerGas='" + getMaxFeePerGas() + '\'' +
                ", maxPriorityFeePerGas='" + getMaxPriorityFeePerGas() + '\'' +
                ", signedHex='" + getSignedHex() + '\'' +
                ", timeStamp=" + getTimeStamp() +
                ", memo='" + getMemo() + '\'' +
                ", signId='" + getSignId() + '\'' +
                ", belongTo='" + getBelongTo() + '\'' +
                ", addition='" + getAddition() + '\'' +
                ", signature='" + getSignature() + '\'' +
                '}';
    }

    public BigDecimal getEstimatedFeeValue() {
        return estimatedFeeValue;
    }

    public void setEstimatedFeeValue(BigDecimal estimatedFeeValue) {
        this.estimatedFeeValue = estimatedFeeValue;
    }

    public BigDecimal getMaxFeeValue() {
        return maxFeeValue;
    }

    public void setMaxFeeValue(BigDecimal maxFeeValue) {
        this.maxFeeValue = maxFeeValue;
    }

    public BigDecimal getMaxFeePerGasValue() {
        return maxFeePerGasValue;
    }

    public void setMaxFeePerGasValue(BigDecimal maxFeePerGasValue) {
        this.maxFeePerGasValue = maxFeePerGasValue;
    }

    public BigDecimal getMaxPriorityFeePerGasValue() {
        return maxPriorityFeePerGasValue;
    }

    public void setMaxPriorityFeePerGasValue(BigDecimal maxPriorityFeePerGasValue) {
        this.maxPriorityFeePerGasValue = maxPriorityFeePerGasValue;
    }

    public BigDecimal getGasLimitValue() {
        return gasLimitValue;
    }

    public void setGasLimitValue(BigDecimal gasLimitValue) {
        this.gasLimitValue = gasLimitValue;
    }

    public BigDecimal getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(BigDecimal amountValue) {
        this.amountValue = amountValue;
    }

    public BigDecimal getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(BigDecimal feeValue) {
        this.feeValue = feeValue;
    }
}
