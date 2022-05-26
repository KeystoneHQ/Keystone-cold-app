package com.keystone.cold.viewmodel.tx;

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

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.fragment.main.solana.model.SolTxData;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.sparrowwallet.hummingbird.registry.solana.SolSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SolTxViewModel extends Base {

    private static final String TAG = SolTxViewModel.class.getSimpleName();

    public static final String KEY_DATA = "data_key";
    public static final String KEY_FROM_ADDRESS = "from_address_key";

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private String txHex;
    private String messageData;
    private String hdPath;
    private int chainId;

    private String requestId;
    private String signature;

    private String parsedMessage;

    private final MutableLiveData<JSONObject> parseMessageJsonLiveData;

    public SolTxViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        coinCode = "SOL";
        parseMessageJsonLiveData = new MutableLiveData<>();
    }

    private final SignCallBack signCallBack = new SignCallBack() {
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
        public void onSignTxSuccess(String signatureHex) {
            signature = signatureHex;
            signState.postValue(STATE_SIGN_SUCCESS);
            insertDB(signature, txHex, parsedMessage);
            new ClearTokenCallable().call();
        }

        @Override
        public void onSignMsgSuccess(String signatureHex) {
            signature = signatureHex;
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        }
    };

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new SolImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.signaturHex);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new SolImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignMsgSuccess(result);
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

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signTransaction(signer);
        });
    }

    public void handleSignPersonalMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signMessage(signer);
        });
    }

    public MutableLiveData<JSONObject> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);

            SolImpl.parseMessage(txHex, new SolImpl.ParseMessageCallback() {
                @Override
                public void onSuccess(String json) {
                    parsedMessage = json;
                    try {
                        ensureAddressExist(hdPath);
                        JSONObject jsonObject = new JSONObject(parsedMessage);
                        parseMessageJsonLiveData.postValue(jsonObject);
                    } catch (InvalidTransactionException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(String error) {
                    Log.e(TAG, String.format("sign error is %s", error));
                    parseMessageJsonLiveData.postValue(null);
                }
            });
        });
    }


    public MutableLiveData<JSONObject> parseRawMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                messageData = bundle.getString(SIGN_DATA);
                hdPath = bundle.getString(HD_PATH);
                requestId = bundle.getString(REQUEST_ID);
                String fromAddress = getFromAddress(hdPath);
                JSONObject object = new JSONObject();
                object.put(HD_PATH, hdPath);
                object.put(REQUEST_ID, requestId);
                object.put(KEY_DATA, messageData);
                object.put(KEY_FROM_ADDRESS, fromAddress);
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                parseTxException.postValue(e);
            }
        });
        return observableObject;
    }

    private String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            AddressEntity addressEntity = mRepository.loadAddressBypath(path);
            if (addressEntity != null) {
                return addressEntity.getAddressString();
            }
        } catch (InvalidTransactionException e) {
            parseTxException.postValue(e);
            e.printStackTrace();
        }
        return "";
    }

    private void ensureAddressExist(String path) throws InvalidTransactionException {
        String code = Utilities.getCurrentSolAccount(context);
        SOLAccount account = SOLAccount.ofCode(code);
        path = path.toUpperCase();
        SOLAccount target = SOLAccount.getAccountByPath(path);
        if (target == null) {
            throw new InvalidTransactionException("unknown hd path");
        }
        AddressEntity address = mRepository.loadAddressBypath(path);
        if (address == null) {
            int addressIndex = getAddressIndex(account, path);
            if (addressIndex != -1) {
                AccountEntity accountEntity = mRepository.loadTargetSOLAccount(SOLAccount.ofCode(Utilities.getCurrentSolAccount(context)));
                if (accountEntity == null) {
                    throw new InvalidTransactionException("not having matched accounts.");
                }
                int addressNum = addressIndex - accountEntity.getAddressLength() + 1;
                CoinEntity coin = mRepository.loadCoinEntityByCoinCode(Coins.SOL.coinCode());
                updateAccountDB(accountEntity, mRepository, addressNum, coin);
            }
        }
    }

    private void updateAccountDB(AccountEntity accountEntity, DataRepository repository, int number, CoinEntity coinEntity) {
        int addressLength = accountEntity.getAddressLength();
        int targetAddressCount = addressLength + number;
        List<AddressEntity> entities = new ArrayList<>();
        for (int index = addressLength; index < targetAddressCount; index++) {
            AddressEntity addressEntity = new AddressEntity();
            String addr = AddAddressViewModel.deriveSolAddress(accountEntity, index, addressEntity);
            if (repository.loadAddressBypath(addressEntity.getPath()) != null) {
                continue;
            }
            addressEntity.setAddressString(addr);
            addressEntity.setCoinId(coinEntity.getCoinId());
            addressEntity.setIndex(index);
            addressEntity.setName("SOL-" + index);
            addressEntity.setBelongTo(coinEntity.getBelongTo());
            entities.add(addressEntity);
        }
        coinEntity.setAddressCount(targetAddressCount);
        accountEntity.setAddressLength(targetAddressCount);
        repository.updateAccount(accountEntity);
        repository.updateCoin(coinEntity);
        repository.insertAddress(entities);
    }

    private int getAddressIndex(SOLAccount account, String path) {
        return account.getAddressIndexByPath(path);
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

    @Override
    public String getTxId() {
        return signature;
    }

    public String getSignatureUR() {
        if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(requestId)) {
            return "";
        }
        byte[] signatureByte = Hex.decode(signature);
        UUID uuid = UUID.fromString(requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        SolSignature solSignature = new SolSignature(signatureByte, requestId);
        return solSignature.toUR().toString();
    }

    public SolTxData parseSolTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin) && coin.equals(Coins.SOL.coinId())) {
                String signature = additions.getJSONObject("addition").getString("signature");
                String rawMessage = additions.getJSONObject("addition").getString("raw_message");
                String parsedMessage = additions.getJSONObject("addition").getString("parsed_message");
                String signBy = additions.getJSONObject("addition").getString("sign_by");
                SolTxData solTxData = new SolTxData();
                solTxData.setSignature(signature);
                solTxData.setRawMessage(rawMessage);
                solTxData.setParsedMessage(parsedMessage);
                solTxData.setSignBy(signBy);
                solTxData.setSignatureUR(txEntity.getSignedHex());
                return solTxData;
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void insertDB(String signature, String rawMessage, String parsedMessage) {
        TxEntity txEntity = generateSolTxEntity();
        txEntity.setTxId(signature);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parsed_message", parsedMessage);
            addition.put("sign_by", Utilities.getCurrentSolAccount(getApplication()));
            JSONObject additions = new JSONObject();
            additions.put("coin", Coins.SOL.coinId());
            additions.put("addition", addition);
            JSONObject root = new JSONObject();
            root.put("additions", additions);
            additionsString = root.toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        if (!TextUtils.isEmpty(additionsString)) {
            //addition结构详见 com.keystone.cold.db.entity.TxEntity addition字段
            txEntity.setAddition(additionsString);
            mRepository.insertTx(txEntity);
        }
    }

    private TxEntity generateSolTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.SOL.coinId());
        txEntity.setSignId(watchWallet.getSignId());
        txEntity.setCoinCode(Coins.SOL.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }
}
