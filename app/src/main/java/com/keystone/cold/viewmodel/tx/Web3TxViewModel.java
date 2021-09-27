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
import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.abi.AbiLoadManager;
import com.keystone.coinlib.abi.Contract;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.ETH.Network;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.ens.EnsLoadManager;
import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.encryption.ChipSigner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Objects;

public class Web3TxViewModel extends Base {
    private String txHex;
    private String messageData;
    private String hdPath;
    private int chainId;

    private String requestId;
    private String signature;

    private JSONArray tokensMap;
    private JSONObject abi;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private boolean isLegacyTypedData;
    private String inputData;
    private boolean isFromTFCard;
    private MutableLiveData<GenericETHTxEntity> observableEthTx = new MutableLiveData<>();
    private SignCallBack signCallBack = new SignCallBack() {
        @Override
        public void startSign() {
            signState.postValue(STATE_SIGNING);
        }

        @Override
        public void onFail() {
            signState.postValue(STATE_SIGN_FAIL);
            new ClearTokenCallable().call();
        }

        @Override
        public void onSignTxSuccess(String txId, String signedTxHex, String signatureHex) {
            signature = signatureHex;
            insertDB(txId, signedTxHex, signatureHex);
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        }

        @Override
        public void onSignMsgSuccess(String signatureHex) {
            signature = signatureHex;
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        }
    };

    public Web3TxViewModel(@NonNull Application application) {
        super(application);
        context = application;
        coinCode = "ETH";
        readPresetContractInfo();
    }

    protected void readPresetContractInfo() {
        try {
            tokensMap = new JSONArray(readAsset("abi/token_address_book.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String loadEnsAddress(String address) {
        EnsLoadManager ensLoadManager = new EnsLoadManager(address);
        return ensLoadManager.load();
    }

    public String recognizeAddress(String to) {
        try {
            String addressSymbol = null;
            for (int i = 0; i < tokensMap.length(); i++) {
                JSONObject token = tokensMap.getJSONObject(i);
                if (token.getString("contract_address").equalsIgnoreCase(to)) {
                    addressSymbol = token.getString("symbol");
                    break;
                }
            }
            if (TextUtils.isEmpty(addressSymbol)) {
                Contract contract = new AbiLoadManager(to).loadAbi();
                addressSymbol = contract.getName();
            }
            if (addressSymbol != null && addressSymbol.length() > 25) {
                addressSymbol = addressSymbol.substring(0, 10) + "..." + addressSymbol.substring(addressSymbol.length() - 10);
            }
            return addressSymbol;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNetwork(int chainId) {
        Network network = Network.getNetwork(chainId);
        if (network == null) {
            return String.format("chainId:%d", chainId);
        }
        String networkName = network.name();
        if (chainId != 1) {
            networkName += String.format(" (%s)", context.getString(R.string.testnet));
        }
        return networkName;
    }

    public MutableLiveData<GenericETHTxEntity> getObservableEthTx() {
        return observableEthTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                txHex = bundle.getString(SIGN_DATA);
                hdPath = bundle.getString(HD_PATH);
                requestId = bundle.getString(REQUEST_ID);
                JSONObject ethTx = EthImpl.decodeTransaction(txHex, () -> isFromTFCard = true);
                if (ethTx == null) {
                    observableEthTx.postValue(null);
                    parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
                    return;
                }
                chainId = ethTx.getInt("chainId");
                String data = ethTx.getString("data");
                try {
                    abi = new JSONObject(data);
                } catch (JSONException ignore) {
                    inputData = data;
                }
                GenericETHTxEntity tx = generateGenericETHTxEntity(ethTx);
                String fromAddress = getFromAddress(hdPath);
                tx.setFrom(fromAddress);
                observableEthTx.postValue(tx);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void parseEIP1559TxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                txHex = bundle.getString(SIGN_DATA);
                requestId = bundle.getString(REQUEST_ID);
                hdPath = bundle.getString(HD_PATH);
                JSONObject ethTx = EthImpl.decodeEIP1559Transaction(txHex, () -> isFromTFCard = true);
                if (ethTx == null) {
                    observableEthTx.postValue(null);
                    parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
                    return;
                }
                chainId = ethTx.getInt("chainId");
                String data = ethTx.getString("data");
                try {
                    abi = new JSONObject(data);
                } catch (JSONException ignore) {
                    inputData = data;
                }
                GenericETHTxEntity tx = generateEIP1559ETHTxEntity(ethTx);
                tx.setFrom(getFromAddress(hdPath));
                observableEthTx.postValue(tx);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public MutableLiveData<JSONObject> parseEIP712TypedData(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                String typedDataHex = bundle.getString(SIGN_DATA);
                hdPath = bundle.getString(HD_PATH);
                requestId = bundle.getString(REQUEST_ID);
                String fromAddress = getFromAddress(hdPath);
                messageData = new String(Hex.decode(typedDataHex), StandardCharsets.UTF_8);
                //LegacyTypedData is a JSON Array;
                isLegacyTypedData = messageData.startsWith("[");
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("requestId", requestId);
                object.put("isLegacy", isLegacyTypedData);
                object.put("data", messageData);
                object.put("fromAddress", fromAddress);
                chainId = 1;
                if (!isLegacyTypedData) {
                    JSONObject typedData = new JSONObject(messageData);
                    chainId = typedData.getJSONObject("domain").optInt("chainId", 1);
                }
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                parseTxException.postValue(e);
            }
        });
        return observableObject;
    }

    public MutableLiveData<JSONObject> parseRawMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                hdPath = bundle.getString(HD_PATH);
                requestId = bundle.getString(REQUEST_ID);
                messageData = bundle.getString(SIGN_DATA);
                String fromAddress = getFromAddress(hdPath);
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("requestId", requestId);
                object.put("data", messageData);
                object.put("fromAddress", fromAddress);
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                parseTxException.postValue(e);
            }
        });
        return observableObject;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signTransaction(signer);
        });
    }

    public void handleSignFeeMarket() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signFeeMarketTransaction(signer);
        });
    }

    public void handleSignEIP712TypedData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signEIP712TypedData(signer);
        });
    }

    public void handleSignPersonalMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signPersonalMessage(signer);
        });
    }

    private GenericETHTxEntity generateGenericETHTxEntity(JSONObject object) throws JSONException {
        GenericETHTxEntity tx = new GenericETHTxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setTo(object.getString("to"));
        BigDecimal amount = new BigDecimal(object.getString("value"));
        double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        tx.setAmount(nf.format(value) + " ETH");
        tx.setFee(nf.format(calculateDisplayFee(object)) + " ETH");
        tx.setMemo(object.getString("data"));
        tx.setBelongTo(mRepository.getBelongTo());
        tx.setTxType(TransactionType.LEGACY.getType());
        return tx;
    }

    private GenericETHTxEntity generateEIP1559ETHTxEntity(JSONObject object) throws JSONException {
        GenericETHTxEntity tx = new GenericETHTxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setTo(object.getString("to"));
        BigDecimal amount = new BigDecimal(object.getString("value"));
        BigDecimal value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        tx.setAmount(nf.format(value) + " ETH");
        calculateDisplayEIP1559Fee(object, tx);
        tx.setMemo(object.getString("data"));
        tx.setBelongTo(mRepository.getBelongTo());
        tx.setTxType(TransactionType.FEE_MARKET.getType());
        return tx;
    }

    private double calculateDisplayFee(JSONObject ethTx) throws JSONException {
        BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        return gasLimit.multiply(gasPrice)
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void calculateDisplayEIP1559Fee(JSONObject ethTx, GenericETHTxEntity tx) throws JSONException {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
        BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        BigDecimal estimatedFee = gasPriorityPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal maxFee = gasLimitPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        tx.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
        tx.setMaxFeePerGas(nf.format(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");
        tx.setGasLimit(nf.format(gasLimit));
        tx.setEstimatedFee(nf.format(estimatedFee) + " ETH");
        tx.setMaxFee(nf.format(maxFee) + " ETH");
    }

    public String getFromAddress(String path) {
        ensureAddressExist(path);
        return mRepository.loadAddressBypath(path).getAddressString();
    }

    private void ensureAddressExist(String path) {
        AddressEntity address = mRepository.loadAddressBypath(path);
        if (address == null) {
            addAddress(getAddressIndex(path));
        }
    }

    private int getAddressIndex(String hdPath) {
        try {
            return CoinPath.parsePath(hdPath).getValue();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getSignatureJson() {
        JSONObject signed = new JSONObject();
        try {
            signed.put("signature", signature);
            signed.put("requestId", requestId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return signed.toString();
    }

    private void insertDB(String txId, String signedTxHex, String signatureHex) {
        GenericETHTxEntity genericETHTxEntity = observableEthTx.getValue();
        if (genericETHTxEntity == null) return;
        try {
            genericETHTxEntity.setTxId(txId);
            genericETHTxEntity.setSignedHex(signedTxHex);
            genericETHTxEntity.setSignature(signatureHex);
            JSONObject addition = new JSONObject();
            addition.put("isFromTFCard", isFromTFCard);
            addition.put("requestId", requestId);
            genericETHTxEntity.setAddition(addition.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRepository.insertETHTx(GenericETHTxEntity.transToDbEntity(genericETHTxEntity));
    }

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.txId, result.txHex, result.signaturHex);
        }
    }

    private void signFeeMarketTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signEIP1559Hex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.txId, result.txHex, result.signaturHex);
        }
    }

    private void signEIP712TypedData(Signer signer) {
        signCallBack.startSign();
        String signatureHex = new EthImpl(chainId).signEIP712TypedData(messageData, signer);
        if (signatureHex == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignMsgSuccess(signatureHex);
        }
    }

    private void signPersonalMessage(Signer signer) {
        signCallBack.startSign();
        String signatureHex = new EthImpl(chainId).signPersonalMessage(messageData, signer);
        if (signatureHex == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignMsgSuccess(signatureHex);
        }
    }

    private Signer initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new ChipSigner(hdPath.toLowerCase(), authToken);
    }

    public String getTxId() {
        return Objects.requireNonNull(observableEthTx.getValue()).getTxId();
    }

    public JSONObject getAbi() {
        return abi;
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableEthTx.getValue()).getSignedHex();
    }

    public int getChainId() {
        return chainId;
    }

    public String getInputData() {
        return inputData;
    }

    public boolean isFromTFCard() {
        return isFromTFCard;
    }

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String txId, String signedTxHex, String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }
}
