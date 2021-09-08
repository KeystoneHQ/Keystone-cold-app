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
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
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
    private JSONArray tokensMap;
    private String hdPath;
    private String signId;
    private String signature;
    private String txHex;
    private String signedTxHex;
    private int chainId;
    private JSONObject abi;
    private String txId;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private String messageData;
    private String fromAddress;
    private String inputData;
    private boolean isFromTFCard;
    private MutableLiveData<GenericETHTxEntity> observableFeeTx = new MutableLiveData<>();

    public Web3TxViewModel(@NonNull Application application) {
        super(application);
        context = application;
        coinCode = Coins.ETH.coinCode();
        readPresetContractInfo();
    }

    protected void readPresetContractInfo() {
        try {
            tokensMap = new JSONArray(readAsset("abi/token_address_book.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getTokensMap() {
        return tokensMap;
    }

    public String loadEnsAddress(String address) {
        EnsLoadManager ensLoadManager = new EnsLoadManager(address);
        return ensLoadManager.load();
    }

    public String recognizeAddress(String to) {
        try {
            String addressSymbol = null;
            JSONArray tokensMap = getTokensMap();
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

    public MutableLiveData<TxEntity> getObservableTx() {
        return observableTx;
    }

    public MutableLiveData<GenericETHTxEntity> getObservableFeeTx() {
        return observableFeeTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                txHex = bundle.getString(SIGN_DATA);
                hdPath = bundle.getString(HD_PATH);
                signId = bundle.getString(REQUEST_ID);
                JSONObject ethTx = EthImpl.decodeTransaction(txHex, () -> isFromTFCard = true);
                if (ethTx == null) {
                    observableTx.postValue(null);
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
                TxEntity tx = generateTxEntity(ethTx);
                observableTx.postValue(tx);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void parseEIP1559TxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                txHex = bundle.getString(SIGN_DATA);
                hdPath = bundle.getString(HD_PATH);
                signId = bundle.getString(REQUEST_ID);
                JSONObject ethTx = EthImpl.decodeEIP1559Transaction(txHex, () -> isFromTFCard = true);
                if (ethTx == null) {
                    observableTx.postValue(null);
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
                observableFeeTx.postValue(tx);
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
                signId = bundle.getString(REQUEST_ID);
                fromAddress = getFromAddress(hdPath);
                messageData = new String(Hex.decode(typedDataHex), StandardCharsets.UTF_8);
                JSONObject typedData = new JSONObject(messageData);
                chainId = typedData.getJSONObject("domain").optInt("chainId", 1);

                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("signId", signId);
                object.put("data", messageData);
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
                signId = bundle.getString(REQUEST_ID);
                messageData = bundle.getString(SIGN_DATA);
                fromAddress = getFromAddress(hdPath);
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("signId", signId);
                object.put("data", messageData);
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                parseTxException.postValue(e);
            }
        });
        return observableObject;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    private TxEntity generateTxEntity(JSONObject object) throws JSONException {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setCoinCode(coinCode);
        fromAddress = getFromAddress(hdPath);
        tx.setFrom(fromAddress);
        tx.setTo(object.getString("to"));
        BigDecimal amount = new BigDecimal(object.getString("value"));
        double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        tx.setAmount(nf.format(value) + " ETH");
        tx.setFee(nf.format(calculateDisplayFee(object)) + " ETH");
        tx.setMemo(object.getString("data"));
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private GenericETHTxEntity generateGenericETHTxEntity(JSONObject object) throws JSONException {
        GenericETHTxEntity tx = new GenericETHTxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        fromAddress = getFromAddress(hdPath);
        tx.setFrom(fromAddress);
        tx.setTo(object.getString("to"));
        BigDecimal amount = new BigDecimal(object.getString("value"));
        double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        tx.setAmount(nf.format(value) + " ETH");
        calculateDisplayEIP1559Fee(object, tx);
        tx.setMemo(object.getString("data"));
        tx.setBelongTo(mRepository.getBelongTo());
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
        BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
        BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        BigDecimal estimatedFee = BigDecimal.valueOf(gasLimitPrice.multiply(gasLimit).doubleValue() - gasPriorityPrice.doubleValue())
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal maxFee = BigDecimal.valueOf(gasLimitPrice.multiply(gasLimit).doubleValue() - gasLimitPrice.doubleValue())
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        tx.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice) + " GWEI");
        tx.setMaxFeePerGas(nf.format(gasLimitPrice) + " GWEI");
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

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            Signer signer = initSigner();
            signTransaction(callback, signer);
        });
    }

    public void handleSignFeeMarket() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignEIP1559TxCallback();
            callback.startSign();
            Signer signer = initSigner();
            signFeeMarketTransaction(callback, signer);
        });
    }

    protected SignCallback initSignEIP1559TxCallback() {
        return new SignCallback() {
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
            public void onSuccess(String txid, String signatureStr) {
                txId = txid;
                signature = signatureStr;
                signState.postValue(STATE_SIGN_SUCCESS);
                insertDB();
                new ClearTokenCallable().call();
            }

            @Override
            public void postProgress(int progress) {

            }
        };
    }

    private SignCallback initialSignMessageCallback() {
        return new SignCallback() {
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
            public void onSuccess(String txId, String sig) {
                signature = sig;
                signState.postValue(STATE_SIGN_SUCCESS);
                new ClearTokenCallable().call();
            }

            @Override
            public void postProgress(int progress) {

            }
        };
    }

    public void handleSignEIP712TypedData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initialSignMessageCallback();
            callback.startSign();
            Signer signer = initSigner();
            signEIP712TypedData(callback, signer);
        });
    }

    public void handleSignPersonalMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initialSignMessageCallback();
            callback.startSign();
            Signer signer = initSigner();
            signPersonalMessage(callback, signer);
        });
    }

    public String getSignatureJson() {
        JSONObject signed = new JSONObject();
        try {
            signed.put("signature", signature);
            signed.put("signId", signId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return signed.toString();
    }

    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTx.getValue();
        this.txId = txId;
        Objects.requireNonNull(tx).setTxId(txId);
        JSONObject signed = new JSONObject();
        signature = EthImpl.getSignature(rawTx);
        try {
            signed.put("signature", signature);
            signed.put("signId", signId);
            signed.put("chainId", chainId);
            signed.put("abi", abi);
            signed.put("inputData", inputData);
            signed.put("isFromTFCard", isFromTFCard);
            tx.setSignedHex(signed.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
        try {
            genericETHTxEntity.setTxId(tx.getTxId());
            genericETHTxEntity.setSignedHex(rawTx);
            genericETHTxEntity.setFrom(fromAddress);
            genericETHTxEntity.setTimeStamp(tx.getTimeStamp());
            genericETHTxEntity.setBelongTo(tx.getBelongTo());
            genericETHTxEntity.setTxType(TransactionType.LEGACY.getType());
            JSONObject addition = new JSONObject();
            addition.put("isFromTFCard", isFromTFCard);
            genericETHTxEntity.setAddition(addition.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRepository.insertETHTx(GenericETHTxEntity.transToDbEntity(genericETHTxEntity));
        return tx;
    }

    private void insertDB() {
        GenericETHTxEntity genericETHTxEntity = observableFeeTx.getValue();
        if (genericETHTxEntity == null) return;
        try {
            genericETHTxEntity.setTxId(txId);
            genericETHTxEntity.setSignedHex(signedTxHex);
            genericETHTxEntity.setFrom(fromAddress);
            genericETHTxEntity.setTxType(TransactionType.FEE_MARKET.getType());
            JSONObject addition = new JSONObject();
            addition.put("isFromTFCard", isFromTFCard);
            genericETHTxEntity.setAddition(addition.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRepository.insertETHTx(GenericETHTxEntity.transToDbEntity(genericETHTxEntity));
    }

    private void signTransaction(@NonNull SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        SignTxResult result = new EthImpl(chainId).signHex(txHex, signer);
        if (result == null) {
            callback.onFail();
        } else {
            callback.onSuccess(result.txId, result.txHex);
        }
    }

    private void signFeeMarketTransaction(@NonNull SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        SignTxResult result = new EthImpl(chainId).signEIP1559Hex(txHex, signer);
        if (result == null) {
            callback.onFail();
        } else {
            txId = result.txId;
            signedTxHex = result.txHex;
            signature = result.signaturHex;
            callback.onSuccess(result.txId, result.txHex);
        }
    }

    private void signEIP712TypedData(@NonNull SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        String sig = new EthImpl(chainId).signEIP712TypedData(messageData, signer);
        if (sig == null) {
            callback.onFail();
        } else {
            callback.onSuccess(sig, sig);
        }
    }

    private void signPersonalMessage(@NonNull SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        String sig = new EthImpl(chainId).signPersonalMessage(messageData, signer);
        if (sig == null) {
            callback.onFail();
        } else {
            callback.onSuccess(sig, sig);
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
        return txId;
    }

    public JSONObject getAbi() {
        return abi;
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableTx.getValue()).getSignedHex();
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
}
