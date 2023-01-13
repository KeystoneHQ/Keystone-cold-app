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

package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import static com.keystone.cold.MainApplication.getApplication;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.tx.TransactionType;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.UUID;

public class EthereumTransaction implements Tx {

    public static int MAX_PER_GAS = 1000;
    public static int MAX_PRIORITY_PER_GAS = 1000;
    public static int MAX_FEE_PER_GAS = 10000;

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
    private long chainId;
    private String addition;
    private int txType; // 0x00:legacy  0x02:feeMarket
    private boolean isFromTFCard;

    // transaction related;
    private String hdPath;
    private String requestId;
    private String txHex;

    // abi related;
    private String toContractName;
    private JSONObject abi;
    private String selectorMethodName;

    public static EthereumTransaction transformDbEntity(Web3TxEntity web3TxEntity) {
        EthereumTransaction transaction = new EthereumTransaction();
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
                    transaction = getGenericETHTxEntity(ethTx, web3TxEntity);
                    transaction.setSignature(EthImpl.getSignature(web3TxEntity.getSignedHex()));
                    long chainId = getChainIdByEIP155(ethTx.getLong("chainId"));
                    transaction.setChainId(chainId);
                    BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
                    gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
                    transaction.setGasLimit(nf.format(gasLimit));
                    transaction.setGasLimitValue(gasLimit);
                    BigDecimal fee = gasLimit.multiply(gasPrice).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    amount = new BigDecimal(ethTx.getString("value"));
                    value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    transaction.setAmountValue(amount);
                    transaction.setAmount(nf.format(value) + Web3TxViewModel.getSymbol(chainId));
                    transaction.setFee(nf.format(fee) + Web3TxViewModel.getSymbol(chainId));
                    transaction.setFeeValue(fee);
                    JSONObject addition = new JSONObject(web3TxEntity.getAddition());
                    transaction.setFromTFCard(addition.getBoolean("isFromTFCard"));
                    transaction.setRequestId(addition.getString("requestId"));
                    break;
                case 0x02:
                    ethTx = EthImpl.decodeEIP1559Transaction(web3TxEntity.getSignedHex(), null);
                    if (ethTx == null) {
                        return null;
                    }
                    transaction = getGenericETHTxEntity(ethTx, web3TxEntity);
                    transaction.setSignature(EthImpl.getEIP1559Signature(web3TxEntity.getSignedHex()));
                    transaction.setChainId(ethTx.getInt("chainId"));
                    BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
                    BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
                    gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
                    BigDecimal estimatedFee = gasPriorityPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    BigDecimal maxFee = gasLimitPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    transaction.setMaxFeeValue(maxFee);
                    transaction.setMaxPriorityFeePerGasValue(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
                    transaction.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
                    transaction.setMaxFeePerGasValue(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
                    transaction.setMaxFeePerGas(nf.format(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
                    transaction.setGasLimit(nf.format(gasLimit));
                    transaction.setEstimatedFeeValue(estimatedFee);
                    transaction.setEstimatedFee(nf.format(estimatedFee) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    transaction.setMaxFee(nf.format(maxFee) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    amount = new BigDecimal(ethTx.getString("value"));
                    value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
                    transaction.setAmount(nf.format(value) + Web3TxViewModel.getSymbol(ethTx.getInt("chainId")));
                    JSONObject additionJson = new JSONObject(web3TxEntity.getAddition());
                    transaction.setFromTFCard(additionJson.getBoolean("isFromTFCard"));
                    transaction.setRequestId(additionJson.getString("requestId"));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transaction;
    }

    public static Web3TxEntity transToDbEntity(EthereumTransaction genericETHTxEntity) {
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

    private static EthereumTransaction getGenericETHTxEntity(JSONObject ethTx, Web3TxEntity web3TxEntity) throws JSONException {
        EthereumTransaction genericETHTxEntity = new EthereumTransaction();
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
        String currentBelongTo = Utilities.getCurrentBelongTo(getApplication());
        genericETHTxEntity.setBelongTo(currentBelongTo);
        return genericETHTxEntity;
    }

    private static long getChainIdByEIP155(long chainId) {
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
        if (this.chainId == 43114) return Coins.AVAX.coinCode();
        if (this.chainId == 9000 || this.chainId == 9001) {
            return Coins.EVMOS.coinCode();
        }
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

    public boolean isFeeExceeded() {
        if (txType == TransactionType.LEGACY.getType()) {
            return getGasPrice(this.getFeeValue(), getGasLimitValue()).doubleValue() > MAX_PER_GAS;
        } else {
            double maxPriorityFee = getMaxPriorityFeePerGasValue().doubleValue();
            boolean isMaxPriorityFeeExceeded = maxPriorityFee > MAX_PRIORITY_PER_GAS;
            double maxfee = getMaxFeePerGasValue().doubleValue();
            boolean isMaxFeeExceeded = maxfee > MAX_FEE_PER_GAS;
            return isMaxPriorityFeeExceeded || isMaxFeeExceeded;
        }
    }

    public BigDecimal getGasPrice(BigDecimal feeValue, BigDecimal limitValue) {
        if (limitValue == null) return new BigDecimal(-1);
        BigDecimal value = feeValue.multiply(BigDecimal.TEN.pow(18));
        return value.divide(limitValue.multiply(BigDecimal.TEN.pow(9)), 8, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String getSignId() {
        try {
            JSONObject jsonObject = new JSONObject(this.addition);
            return jsonObject.optString("signId", WatchWallet.METAMASK_SIGN_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return WatchWallet.METAMASK_SIGN_ID;
        }
    }

    public String getHdPath() {
        return hdPath;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setHdPath(String hdPath) {
        this.hdPath = hdPath;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setToContractName(String toContractName) {
        this.toContractName = toContractName;
    }

    public void setAbi(JSONObject abi) {
        this.abi = abi;
    }

    public void setSelectorMethodName(String selectorMethodName) {
        this.selectorMethodName = selectorMethodName;
    }

    public String getToContractName() {
        return toContractName;
    }

    public JSONObject getAbi() {
        return abi;
    }

    public String getSelectorMethodName() {
        return selectorMethodName;
    }

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
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

    public String getSignatureQRCode() {
        try {
            byte[] signature = Hex.decode(getSignature());
            JSONObject addition = new JSONObject(getAddition());
            UUID uuid = UUID.fromString(getRequestId());
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            EthSignature ethSignature = new EthSignature(signature, requestId);
            return ethSignature.toUR().toString();
        } catch (Exception e) {
            e.printStackTrace();
            byte[] signature = Hex.decode(getSignature());
            EthSignature ethSignature = new EthSignature(signature);
            return ethSignature.toUR().toString();
        }
    }

    public long getChainId() {
        return chainId;
    }

    public int getIcon() {
        if (this.chainId == 1) return R.drawable.coin_eth;
        if (this.chainId == 43114) return R.drawable.coin_avax;
        if (this.chainId == 9000 || this.chainId == 9001) return R.drawable.coin_evmos;
        return R.drawable.coin_eth_token;
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

    public void setChainId(long chainId) {
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

    @Override
    public String toString() {
        return "GenericETHTxEntity{" +
                "txId='" + txId + '\'' +
                ", amount='" + amount + '\'' +
                ", amountValue=" + amountValue +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", fee='" + fee + '\'' +
                ", feeValue=" + feeValue +
                ", signedHex='" + signedHex + '\'' +
                ", timeStamp=" + timeStamp +
                ", memo='" + memo + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", estimatedFee='" + estimatedFee + '\'' +
                ", maxFee='" + maxFee + '\'' +
                ", maxFeePerGas='" + maxFeePerGas + '\'' +
                ", maxPriorityFeePerGas='" + maxPriorityFeePerGas + '\'' +
                ", gasLimit='" + gasLimit + '\'' +
                ", estimatedFeeValue=" + estimatedFeeValue +
                ", maxFeeValue=" + maxFeeValue +
                ", maxFeePerGasValue=" + maxFeePerGasValue +
                ", maxPriorityFeePerGasValue=" + maxPriorityFeePerGasValue +
                ", gasLimitValue=" + gasLimitValue +
                ", signature='" + signature + '\'' +
                ", chainId=" + chainId +
                ", addition='" + addition + '\'' +
                ", txType=" + txType +
                ", isFromTFCard=" + isFromTFCard +
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

    public enum TransactionType {
        LEGACY(0x00),
        FEE_MARKET(0x02);

        private int type;

        TransactionType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

    }
}
