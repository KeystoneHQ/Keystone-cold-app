package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;

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
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.ens.EnsLoadManager;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.helper.CoinConfigHelper;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EthereumTxViewModel extends BaseTxViewModel<EthereumTransaction> {
    private static final String TAG = "EthereumTxViewModel";

    protected final DataRepository mRepository;

    private String messageData;

    private long chainId;

    private String hdPath;
    private String signData;
    private int txType;

    private String requestId;
    private String signature;

    private boolean isExceeded;

    private static JSONArray tokensMap;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private boolean isLegacyTypedData;

    public boolean isExceeded() {
        return isExceeded;
    }

    public void setExceeded(boolean exceeded) {
        isExceeded = exceeded;
    }


    private static JSONObject chainIdJSONObject;
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
            insertDB(txId, signedTxHex);
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

    static {
        try {
            chainIdJSONObject = new JSONObject(readAsset("chain/chainId.json"));
            tokensMap = new JSONArray(readAsset("abi/token_address_book.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public EthereumTxViewModel(@NonNull Application application) {
        super(application);
        context = application;
        mRepository = MainApplication.getApplication().getRepository();
    }

    @Override
    public void parseTxData(Bundle bundle) {

    }

    public static String loadEnsAddress(String address) {
        EnsLoadManager ensLoadManager = new EnsLoadManager(address);
        return ensLoadManager.load();
    }

    public static String recognizeAddress(EthereumTransaction transaction, String to) {
        try {
            String addressSymbol = null;
            for (int i = 0; i < tokensMap.length(); i++) {
                JSONObject token = tokensMap.getJSONObject(i);
                if (token.getString("contract_address").equalsIgnoreCase(to)) {
                    addressSymbol = token.getString("symbol");
                    break;
                }
            }
            if (TextUtils.isEmpty(addressSymbol) && transaction != null) {
                if (!TextUtils.isEmpty(transaction.getTo()) && transaction.getTo().equalsIgnoreCase(to)) {
                    addressSymbol = transaction.getToContractName();
                } else {
                    List<Contract> contracts = new AbiLoadManager(to).loadAbi();
                    if (!contracts.isEmpty()) {
                        addressSymbol = contracts.get(0).getName();
                    }
                }
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

    public static String getNetwork(long chainId) {
        String networkName = "";
        if (chainIdJSONObject != null) {
            JSONObject jsonObject = chainIdJSONObject.optJSONObject(String.valueOf(chainId));
            if (jsonObject != null) {
                networkName = jsonObject.optString("name");
            }
        }
        if (networkName.isEmpty()) {
            return String.format("chainId:%d", chainId);
        } else {
            return networkName;
        }
    }

    // used for get icon when we don't treat this chain as a coin in my assets page.
    public static String getIconCode(long chainId) {
        if (chainId == 1) {
            return Coins.ETH.coinCode();
        }
        if (chainId == 9000 || chainId == 9001) {
            return Coins.EVMOS.coinCode();
        }
        return CoinConfigHelper.getNonEVMChainIconCode(chainId);
    }

    public void reset() {
        observableTransaction.postValue(null);
    }

    public void generateUnsignedTransaction(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            isParsing.postValue(true);
            signData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            rawFormatTx.postValue(signData);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            txType = bundle.getInt(BundleKeys.ETH_TX_TYPE_KEY);
            String fromAddress = getFromAddress(hdPath);
            EthereumTransaction transaction = null;
            switch (txType) {
                case 0x00: {
                    transaction = EthereumTransaction.generateLegacyTransaction(signData, fromAddress, false);
                    break;
                }
                case 0x02: {
                    transaction = EthereumTransaction.generateFeeMarketTransaction(signData, fromAddress);
                    break;
                }
                default:
                    break;
            }

            if (transaction == null) {
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }
            chainId = transaction.getChainId();
            isParsing.postValue(false);
            observableTransaction.postValue(transaction);
        });
    }

    public void generateSignedTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Web3TxEntity txEntity = mRepository.loadETHTxSync(txId);
            txType = txEntity.getTxType();
            signData = txEntity.getSignedHex();
            rawFormatTx.postValue(signData);
            EthereumTransaction transaction = null;
            String fromAddress = txEntity.getFrom();
            switch (txType) {
                case 0x00: {
                    transaction = EthereumTransaction.generateLegacyTransaction(signData, fromAddress, true);
                    signature = EthImpl.getSignature(txEntity.getSignedHex());
                    break;
                }
                case 0x02: {
                    transaction = EthereumTransaction.generateFeeMarketTransaction(signData, fromAddress);
                    signature = EthImpl.getEIP1559Signature(txEntity.getSignedHex());
                    break;
                }
                default:
                    break;
            }
            if (transaction == null) {
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }
            chainId = transaction.getChainId();
            //override values
            transaction.setTxId(txId);
            try {
                JSONObject addition = new JSONObject(txEntity.getAddition());
                requestId = addition.getString("requestId");
                transaction.setFromTFCard(addition.getBoolean("isFromTFCard"));
            } catch (JSONException e) {
                e.printStackTrace();
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }

            transaction.setSignatureUR(getSignatureUR());
            observableTransaction.postValue(transaction);
        });
    }

    public MutableLiveData<JSONObject> parseEIP712TypedData(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                String typedDataHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
                requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
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
                    chainId = typedData.getJSONObject("domain").optLong("chainId", 1);
                }
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                observableException.postValue(new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
        return observableObject;
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
                requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
                messageData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
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
                observableException.postValue(new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
        return observableObject;
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner(hdPath);
            switch (txType) {
                case 0x00: {
                    signTransaction(signer);
                    break;
                }
                case 0x02: {
                    signFeeMarketTransaction(signer);
                    break;
                }
            }
        });
    }

    @Override
    public String getSignatureUR() {
        EthSignature ethSignature;
        byte[] signature = Hex.decode(this.signature);
        if (this.requestId == null) {
            ethSignature = new EthSignature(signature);
        } else {
            UUID uuid = UUID.fromString(this.requestId);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            ethSignature = new EthSignature(signature, requestId);
        }
        return ethSignature.toUR().toString();
    }

    public void handleSignEIP712TypedData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner(hdPath);
            signEIP712TypedData(signer);
        });
    }

    @Override
    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner(hdPath);
            signPersonalMessage(signer);
        });
    }

    public String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            return mRepository.loadAddressByPathAndCoinId(path, Coins.ETH.coinId()).getAddressString();
        } catch (InvalidTransactionException e) {
            observableException.postValue(e);
            e.printStackTrace();
        }
        return "";
    }

    private void ensureAddressExist(String path) throws InvalidTransactionException {
        path = path.toUpperCase();
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setPath(path);
        ETHAccount target = ETHAccount.getAccountByPath(path);
        if (target == null) {
            throw new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "unknown hd path");
        }
        AddressEntity address = mRepository.loadAddressByPathAndCoinId(path, Coins.ETH.coinId());
        if (address == null) {
            updateAccountDb(getAddressIndex(target, path), target);
        }
    }

    private int getAddressIndex(ETHAccount account, String hdPath) {
        int index = 0;
        switch (account) {
            case LEDGER_LIVE:
                hdPath = hdPath.replace("'", "");
                String[] split = hdPath.split("/");
                index = Integer.parseInt(split[3]);
                break;
            case LEDGER_LEGACY:
                hdPath = hdPath.replace("'", "");
                String[] strings = hdPath.split("/");
                index = Integer.parseInt(strings[4]);
                break;
            case BIP44_STANDARD:
                try {
                    index = CoinPath.parsePath(hdPath).getValue();
                } catch (com.keystone.coinlib.exception.InvalidPathException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return index;
    }

    protected void updateAccountDb(int addressIndex, ETHAccount account) throws InvalidTransactionException {
        AccountEntity accountEntity = mRepository.loadTargetETHAccount(account);
        if (accountEntity == null) {
            throw new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "not have match account");
        }
        CoinEntity coin = mRepository.loadCoinEntityByCoinCode(Coins.ETH.coinCode());
        List<AddressEntity> addressEntities = new ArrayList<>();
        for (int i = accountEntity.getAddressLength(); i < addressIndex + 1; i++) {
            AddressEntity addressEntity = new AddressEntity();
            String addr = AddAddressViewModel.deriveETHAddress(accountEntity, i, addressEntity);
            addressEntity.setAddressString(addr);
            addressEntity.setCoinId(Coins.ETH.coinId());
            addressEntity.setIndex(addressIndex);
            addressEntity.setName("ETH-" + i);
            addressEntity.setBelongTo(mRepository.getBelongTo());
            addressEntities.add(addressEntity);
            accountEntity.setAddressLength(accountEntity.getAddressLength() + 1);
            if (ETHAccount.isStandardChildren(addressEntity.getPath())) {
                coin.setAddressCount(coin.getAddressCount() + 1);
            }
        }
        mRepository.updateAccount(accountEntity);
        mRepository.insertAddress(addressEntities);
        mRepository.updateCoin(coin);
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

    private void insertDB(String txId, String signedTxHex) {
        EthereumTransaction transaction = observableTransaction.getValue();
        if (transaction == null) return;
        try {

            Web3TxEntity web3TxEntity = new Web3TxEntity();
            web3TxEntity.setTxId(txId);
            web3TxEntity.setSignedHex(signedTxHex);
            web3TxEntity.setFrom(transaction.getFrom());
            web3TxEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
            web3TxEntity.setBelongTo(mRepository.getBelongTo());
            web3TxEntity.setTxType(txType);

            JSONObject addition = new JSONObject();
            addition.put("isFromTFCard", transaction.isFromTFCard());
            addition.put("requestId", requestId);
            addition.put("signId", Wallet.UNKNOWN_WALLET_SIGN_ID);
            addition.put("signBy", ETHAccount.getAccountByPath(hdPath).getCode());

            web3TxEntity.setAddition(addition.toString());
            mRepository.insertETHTx(web3TxEntity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signHex(signData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.txId, result.txHex, result.signaturHex);
        }
    }

    private void signFeeMarketTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signEIP1559Hex(signData, signer);
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

    private Signer initSigner(String hdPath) {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new RustSigner(hdPath.toLowerCase(), authToken);
    }

    public static String getSymbol(long chainId) {
        String symbol = " ";
        if (chainIdJSONObject != null) {
            JSONObject jsonObject = chainIdJSONObject.optJSONObject(String.valueOf(chainId));
            if (jsonObject != null) {
                symbol += jsonObject.optString("symbol");
            }
        }
        return symbol;
    }

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String txId, String signedTxHex, String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }
}
