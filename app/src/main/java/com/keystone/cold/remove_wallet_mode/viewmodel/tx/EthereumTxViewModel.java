package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;
import static com.keystone.cold.MainApplication.getApplication;
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
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.ens.EnsLoadManager;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.selector.MethodSignature;
import com.keystone.coinlib.selector.SelectorLoadManager;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidETHAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EthereumTxViewModel extends BaseTxViewModel {
    private static final String TAG = "EthereumTxViewModel";

    protected final DataRepository mRepository;

    private String messageData;
    private String hdPath;
    private long chainId;

    private String requestId;
    private String signature;

    private boolean isExceeded;

    private static JSONArray tokensMap;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private boolean isLegacyTypedData;
    private MutableLiveData<EthereumTransaction> observableEthTx = new MutableLiveData<>();
    protected final MutableLiveData<BaseException> parseTxException = new MutableLiveData<>();


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

    public String recognizedSelector(String signature) {
        List<MethodSignature> methodSignatures = new SelectorLoadManager(signature).loadSelector();
        StringBuilder methods = new StringBuilder();
        for (MethodSignature methodSignature : methodSignatures) {
            methods.append(methodSignature.getMethodName()).append(" ");
        }
        return methods.toString();
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

    public void reset() {
        observableEthTx.postValue(null);
    }

    public MutableLiveData<EthereumTransaction> getObservableEthTx() {
        return observableEthTx;
    }

    public MutableLiveData<BaseException> parseTxException() {
        return parseTxException;
    }

    public void generateUnsignedTransaction(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            rawFormatTx.postValue(txHex);
            String hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            String requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            int transactionType = bundle.getInt(BundleKeys.ETH_TX_TYPE_KEY);
            String fromAddress = getFromAddress(hdPath);
            EthereumTransaction transaction = new EthereumTransaction();
            transaction.setTxType(transactionType);
            JSONObject ethTx = null;
            switch (transaction.getTxType()) {
                case 0x00: {
                    ethTx = EthImpl.decodeTransaction(txHex, () -> transaction.setFromTFCard(true));
                    break;
                }
                case 0x02: {
                    ethTx = EthImpl.decodeEIP1559Transaction(txHex, () -> transaction.setFromTFCard(true));
                    break;
                }
                default:
                    break;
            }
            if (ethTx == null) {
                parseTxException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }
            try {
                transaction.setTxHex(txHex);
                transaction.setHdPath(hdPath);
                transaction.setFrom(fromAddress);
                transaction.setRequestId(requestId);

                transaction.setChainId(ethTx.getLong("chainId"));
                transaction.setTo(ethTx.getString("to"));
                if (ethTx.has("to") && ethTx.has("contract")) {
                    transaction.setToContractName(ethTx.getString("contract"));
                }
                transaction.setMemo(ethTx.getString("data"));
                try {
                    transaction.setAbi(new JSONObject(transaction.getMemo()));
                } catch (JSONException e) {
                    transaction.setSelectorMethodName(ethTx.optString("selectorMethodName"));
                }

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                transaction.setTimeStamp(getUniversalSignIndex(getApplication()));
                BigDecimal amount = new BigDecimal(ethTx.getString("value"));
                double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();

                transaction.setAmountValue(amount);
                transaction.setAmount(nf.format(value) + getSymbol(chainId));
                transaction.setBelongTo(mRepository.getBelongTo());

                switch (transaction.getTxType()) {
                    case 0x00: {
                        transaction.setFeeValue(BigDecimal.valueOf(calculateDisplayFee(ethTx)));
                        transaction.setFee(nf.format(calculateDisplayFee(ethTx)) + getSymbol(chainId));
                        transaction.setGasLimit(ethTx.getString("gasLimit"));
                        break;
                    }
                    case 0x02: {
                        calculateDisplayEIP1559Fee(ethTx, transaction);
                    }
                }

            } catch (JSONException e) {
                parseTxException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }
            observableEthTx.postValue(transaction);
        });
    }

    public void generateSignedTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Web3TxEntity txEntity = mRepository.loadETHTxSync(txId);
            EthereumTransaction transaction = EthereumTransaction.transformDbEntity(txEntity);
            rawFormatTx.postValue(transaction.getSignedHex());
            observableEthTx.postValue(transaction);
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
                parseTxException.postValue(new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "invalid transaction"));
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
                parseTxException.postValue(new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
        return observableObject;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            EthereumTransaction transaction = observableEthTx.getValue();
            if (transaction == null) {
                return;
            }
            Signer signer = initSigner(transaction.getHdPath());
            switch (transaction.getTxType()) {
                case 0x00: {
                    signTransaction(transaction, signer);
                    break;
                }
                case 0x02: {
                    signFeeMarketTransaction(transaction, signer);
                    break;
                }
            }
        });
    }

    @Override
    public String getSignatureUR() {
        EthereumTransaction transaction = observableEthTx.getValue();
        if (transaction == null ) {
            //sign typed data
            byte[] signature = Hex.decode(this.signature);
            UUID uuid = UUID.fromString(this.requestId);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            EthSignature ethSignature = new EthSignature(signature, requestId);
            return ethSignature.toUR().toString();
        }
        else {
            return observableEthTx.getValue().getSignatureQRCode();
        }
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

    private double calculateDisplayFee(JSONObject ethTx) throws JSONException {
        BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        return gasLimit.multiply(gasPrice)
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void calculateDisplayEIP1559Fee(JSONObject ethTx, EthereumTransaction tx) throws JSONException {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
        BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        BigDecimal estimatedFee = gasPriorityPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal maxFee = gasLimitPrice.multiply(gasLimit).divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        tx.setMaxPriorityFeePerGasValue(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
        tx.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");

        tx.setMaxFeePerGasValue(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP));
        tx.setMaxFeePerGas(nf.format(gasLimitPrice.divide(BigDecimal.TEN.pow(9), 8, BigDecimal.ROUND_HALF_UP)) + " GWEI");

        tx.setGasLimitValue(gasLimit);
        tx.setGasLimit(nf.format(gasLimit));

        tx.setEstimatedFeeValue(estimatedFee);
        tx.setEstimatedFee(nf.format(estimatedFee) + getSymbol(chainId));

        tx.setMaxFeeValue(maxFee);
        tx.setMaxFee(nf.format(maxFee) + getSymbol(chainId));
    }

    public String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            return mRepository.loadAddressBypath(path).getAddressString();
        } catch (InvalidTransactionException | InvalidETHAccountException e) {
            parseTxException.postValue(e);
            e.printStackTrace();
        }
        return "";
    }

    private void ensureAddressExist(String path) throws InvalidETHAccountException, InvalidTransactionException {
        String code = Utilities.getCurrentEthAccount(context);
        ETHAccount account = ETHAccount.ofCode(code);
        path = path.toUpperCase();
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setPath(path);
        ETHAccount target = ETHAccount.getAccountByPath(path);
        if (target == null) {
            throw new InvalidTransactionException(context.getString(R.string.incorrect_tx_data), "unknown hd path");
        }
        AddressEntity address = mRepository.loadAddressBypath(path);
        if (address == null) {
            updateAccountDb(getAddressIndex(account, path));
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

    protected void updateAccountDb(int addressIndex) throws InvalidTransactionException {
        AccountEntity accountEntity = mRepository.loadTargetETHAccount(ETHAccount.ofCode(Utilities.getCurrentEthAccount(context)));
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

    private void insertDB(String txId, String signedTxHex, String signatureHex) {
        EthereumTransaction transaction = observableEthTx.getValue();
        if (transaction == null) return;
        try {
            transaction.setTxId(txId);
            transaction.setSignedHex(signedTxHex);
            transaction.setSignature(signatureHex);
            JSONObject addition = new JSONObject();
            addition.put("isFromTFCard", transaction.isFromTFCard());
            addition.put("requestId", transaction.getRequestId());
            // TODO update sign ID;
            // addition.put("signId", watchWallet.getSignId());
            addition.put("signBy", ETHAccount.ofCode(Utilities.getCurrentEthAccount(context)).getCode());
            transaction.setAddition(addition.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRepository.insertETHTx(EthereumTransaction.transToDbEntity(transaction));
    }

    private void signTransaction(EthereumTransaction transaction, Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signHex(transaction.getTxHex(), signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.txId, result.txHex, result.signaturHex);
        }
    }

    private void signFeeMarketTransaction(EthereumTransaction transaction, Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new EthImpl(chainId).signEIP1559Hex(transaction.getTxHex(), signer);
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
