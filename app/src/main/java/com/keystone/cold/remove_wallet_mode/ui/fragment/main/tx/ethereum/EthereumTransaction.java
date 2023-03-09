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

import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel.getSymbol;

import androidx.annotation.Nullable;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.CoinConfigHelper;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class EthereumTransaction {

    public static int MAX_PER_GAS = 1000;
    public static int MAX_PRIORITY_PER_GAS = 1000;
    public static int MAX_FEE_PER_GAS = 10000;

    // transaction ui
    private String txId;
    private String amount;
    private String from;
    private String to;
    private String fee;
    private String memo;
    private String gasLimit;
    private String estimatedFee;
    private String maxFee;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;

    // feeMarket params
    private BigDecimal feeValue;
    private BigDecimal estimatedFeeValue;
    private BigDecimal maxFeeValue;
    private BigDecimal maxFeePerGasValue;
    private BigDecimal maxPriorityFeePerGasValue;
    private BigDecimal gasLimitValue;

    // another params
    private long chainId;
    private int txType; // 0x00:legacy  0x02:feeMarket
    private boolean isFromTFCard;

    // abi related;
    private String toContractName;
    private JSONObject abi;
    private String selectorMethodName;

    private String signatureUR;

    private final static NumberFormat nf = NumberFormat.getInstance();

    static {
        nf.setMaximumFractionDigits(20);
    }

    public static EthereumTransaction generateLegacyTransaction(String txHex, String from, boolean isSigned) {
        EthereumTransaction transaction = new EthereumTransaction();
        transaction.from = from;
        transaction.txType = TransactionType.LEGACY.getType();
        JSONObject ethTx = EthImpl.decodeTransaction(txHex, () -> {
            transaction.isFromTFCard = true;
        });
        if (ethTx == null) return null;
        try {
            if (isSigned) {
                transaction.chainId = getChainIdByEIP155(ethTx.getLong("chainId"));
            } else {
                transaction.chainId = ethTx.getLong("chainId");
            }
            String symbol = getSymbol(transaction.chainId);
            setupCommonValue(transaction, ethTx, symbol);

            BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
            String gasLimit = ethTx.getString("gasLimit");
            BigDecimal gasLimitValue = new BigDecimal(gasLimit);
            double feeValue = gasLimitValue.multiply(gasPrice)
                    .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();

            transaction.feeValue = BigDecimal.valueOf(feeValue);
            transaction.fee = nf.format(feeValue) + symbol;
            transaction.gasLimit = gasLimit;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return transaction;
    }

    public static EthereumTransaction generateFeeMarketTransaction(String txHex, String from) {
        EthereumTransaction transaction = new EthereumTransaction();
        transaction.from = from;
        transaction.txType = TransactionType.FEE_MARKET.getType();
        JSONObject ethTx = EthImpl.decodeEIP1559Transaction(txHex, () -> {
            transaction.isFromTFCard = true;
        });
        if (ethTx == null) return null;
        try {
            transaction.chainId = ethTx.getLong("chainId");
            String symbol = getSymbol(transaction.chainId);
            setupCommonValue(transaction, ethTx, symbol);

            BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
            BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
            BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
            BigDecimal estimatedFee = gasPriorityPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
            BigDecimal maxFee = gasLimitPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
            transaction.maxPriorityFeePerGasValue = gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP);
            transaction.maxPriorityFeePerGas = nf.format(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI";

            transaction.maxFeePerGasValue = gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP);
            transaction.maxFeePerGas = nf.format(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI";

            transaction.gasLimitValue = gasLimit;
            transaction.gasLimit = nf.format(gasLimit);

            transaction.estimatedFeeValue = estimatedFee;
            transaction.estimatedFee = nf.format(estimatedFee) + symbol;

            transaction.maxFeeValue = maxFee;
            transaction.maxFee = nf.format(maxFee) + symbol;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return transaction;
    }

    private static void setupCommonValue(EthereumTransaction transaction, JSONObject ethTx, String symbol) throws JSONException {
        transaction.to = ethTx.getString("to");
        if (ethTx.has("to") && ethTx.has("contract")) {
            transaction.toContractName = ethTx.getString("contract");
        }
        transaction.memo = ethTx.getString("data");
        try {
            transaction.abi = new JSONObject(transaction.memo);
        } catch (JSONException e) {
            transaction.selectorMethodName = ethTx.optString("selectorMethodName");
        }

        BigDecimal amount = new BigDecimal(ethTx.getString("value"));
        double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        transaction.amount = nf.format(value) + symbol;
    }

    private static long getChainIdByEIP155(long chainId) {
        return (chainId - 8 - 27) / 2;
    }

    public String getTxId() {
        return txId;
    }

    public String getDisplayName() {
        return Coins.ETH.coinCode();
    }

    public String getAmount() {
        return amount;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public boolean isFeeExceeded() {
        if (txType == TransactionType.LEGACY.getType()) {
            if (gasLimitValue == null) {
                return false;
            }
            BigDecimal value = feeValue.multiply(BigDecimal.TEN.pow(18));
            BigDecimal gasPrice = value.divide(gasLimitValue.multiply(BigDecimal.TEN.pow(9)), 8, BigDecimal.ROUND_HALF_UP);
            return gasPrice.doubleValue() > MAX_PER_GAS;
        } else {
            double maxPriorityFee = maxPriorityFeePerGasValue.doubleValue();
            boolean isMaxPriorityFeeExceeded = maxPriorityFee > MAX_PRIORITY_PER_GAS;
            double maxfee = maxFeePerGasValue.doubleValue();
            boolean isMaxFeeExceeded = maxfee > MAX_FEE_PER_GAS;
            return isMaxPriorityFeeExceeded || isMaxFeeExceeded;
        }
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

    public long getChainId() {
        return chainId;
    }

    @Nullable
    public AssetItem getAssetItem() {
        if (this.chainId == 1) {
            CoinEntity eth = new CoinEntity();
            eth.setCoinCode(Coins.ETH.coinCode());
            eth.setCoinId(Coins.ETH.coinId());
            eth.setName(Coins.ETH.coinName());
            return new AssetItem(eth);
        }
        if (this.chainId == 9000 || this.chainId == 9001) {
            CoinEntity evmos = new CoinEntity();
            evmos.setCoinCode(Coins.EVMOS.coinCode());
            evmos.setCoinId(Coins.EVMOS.coinId());
            evmos.setName(Coins.EVMOS.coinName());
            return new AssetItem(evmos);
        }
        return CoinConfigHelper.getEVMChainByChainID(chainId);
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

    public void setFromTFCard(boolean fromTFCard) {
        isFromTFCard = fromTFCard;
    }

    public String getSignatureUR() {
        return signatureUR;
    }

    public void setSignatureUR(String signatureUR) {
        this.signatureUR = signatureUR;
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
