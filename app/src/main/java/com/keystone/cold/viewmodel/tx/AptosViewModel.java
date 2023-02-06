package com.keystone.cold.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.ORIGIN_DATA;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.cryptocore.AptosParser;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.coinlib.coins.APT.AptImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTx;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTxData;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTxParser;
import com.keystone.cold.util.AptosTransactionHelper;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AptosViewModel extends Base {

    private static final String TAG =  "AptosViewModel";

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private String txHex;
    private String messageData;
    private String hdPath;
    private String origin;

    private String requestId;
    private String signature;
    private String parseJson;

    private String xPub;

    private final MutableLiveData<JSONObject> parseMessageJsonLiveData;
    private final MutableLiveData<AptosTxData> aptosTxDataMutableLiveData;
    private final MutableLiveData<AptosTx> aptosTxLiveData;


    public AptosViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        coinCode = "APTOS";
        parseMessageJsonLiveData = new MutableLiveData<>();
        aptosTxDataMutableLiveData = new MutableLiveData<>();
        aptosTxLiveData = new MutableLiveData<>();
    }

    public LiveData<AptosTx> getAptosTxLiveData() {
        return aptosTxLiveData;
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
            new ClearTokenCallable().call();
            insertDB(signature, txHex, parseJson);
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
        SignTxResult result = new AptImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.signaturHex);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new AptImpl().signMessage(messageData, signer);
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


    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signMessage(signer);
        });
    }

    public MutableLiveData<JSONObject> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    public MutableLiveData<AptosTxData> getAptosTxDataMutableLiveData() {
        return aptosTxDataMutableLiveData;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            origin = bundle.getString(ORIGIN_DATA);
            String data = AptosTransactionHelper.getPureSignData(txHex);
            String parseResult = AptosParser.parse(data);
            Log.i(TAG, "raw is " + parseResult);
            JSONObject jsonObject;

            if (parseResult != null) {
                try {
                    JSONObject rawObject = new JSONObject(parseResult);
                    String aptosExploreFormat = AptosTxParser.convertAptosExploreFormat(rawObject.getJSONObject("formatted_json").toString());
                    Log.i(TAG, "format result is " + aptosExploreFormat);
                    jsonObject = new JSONObject(aptosExploreFormat);
                    AptosTx aptosTx = AptosTxParser.parse(aptosExploreFormat);
                    aptosTxLiveData.postValue(aptosTx);
                    Log.i(TAG, "aptosTx is " + aptosTx);
                } catch (JSONException exception) {
                    exception.printStackTrace();
                    jsonObject = new JSONObject();
                }
            } else {
                jsonObject = new JSONObject();
            }

            parseMessageJsonLiveData.postValue(jsonObject);
            xPub = getXpubByPath(hdPath);
            parseJson = jsonObject.toString();
        });
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


    public String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            return mRepository.loadAddressBypath(path).getAddressString();
        } catch (InvalidTransactionException e) {
            parseTxException.postValue(e);
            e.printStackTrace();
        }
        return "";
    }


    private void ensureAddressExist(String path) throws InvalidTransactionException {
        path = path.toUpperCase();
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setPath(path);
        AddressEntity address = mRepository.loadAddressBypath(path);
        if (address == null) {
            updateAccountDb(getAddressIndex(path));
        }
    }


    private int getAddressIndex(String hdPath) {
        int index = 0;
        hdPath = hdPath.toUpperCase();
        if (!hdPath.startsWith("M/")){
            hdPath = "M/"+ hdPath;
        }
        hdPath = hdPath.replace("'", "");
        String[] strings = hdPath.split("/");
        if (strings.length == 6){
            index = Integer.parseInt(strings[4]);
        }
        return index;
    }

    protected void updateAccountDb(int addressIndex) throws InvalidTransactionException {

        CoinEntity coin = mRepository.loadCoinEntityByCoinCode(Coins.APTOS.coinCode());
        List<AccountEntity> accounts= mRepository.loadAccountsForCoin(coin);
        if (accounts == null || accounts.size() == 0) {
            throw new InvalidTransactionException("not have match account");
        }
        List<AddressEntity> addressEntities = new ArrayList<>();
        for (int i = accounts.get(0).getAddressLength(); i < addressIndex + 1; i++) {
            AddressEntity addressEntity = new AddressEntity();

            String addr = AddAddressViewModel.deriveAptosAddress(i, 0, addressEntity);
            addressEntity.setAddressString(addr);
            addressEntity.setCoinId(Coins.APTOS.coinId());
            addressEntity.setIndex(addressIndex);
            addressEntity.setName("APTOS-" + i);
            addressEntity.setBelongTo(coin.getBelongTo());
            addressEntities.add(addressEntity);
            accounts.get(0).setAddressLength(accounts.get(0).getAddressLength() + 1);
            if (ETHAccount.isStandardChildren(addressEntity.getPath())) {
                coin.setAddressCount(coin.getAddressCount() + 1);
            }
        }
        mRepository.updateAccount(accounts.get(0));
        mRepository.insertAddress(addressEntities);
        mRepository.updateCoin(coin);
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
        byte[] publicKey = null;
        if (xPub != null) {
            publicKey = getPublicKey(xPub);
        }
        AptosSignature aptosSignature = new AptosSignature(signatureByte, requestId, publicKey);
        return aptosSignature.toUR().toString();
    }


    private String getXpubByPath(String path) {
        DataRepository repository = MainApplication.getApplication().getRepository();
        AddressEntity addressEntity = repository.loadAddressBypath(path);
        if (addressEntity != null) {
            String addition = addressEntity.getAddition();
            try {
                JSONObject rootJson = new JSONObject(addition);
                JSONObject additionJson = rootJson.getJSONObject("addition");
                return additionJson.getString("xPub");
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    private byte[] getPublicKey(String xPub) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        byte[] key = extendedPublicKey.getKey();
        byte[] publicKey = new byte[32];
        System.arraycopy(key, 1, publicKey, 0, 32);
        return publicKey;
    }

    private void insertDB(String signature, String rawMessage, String parseJson) {
        TxEntity txEntity = generateAptosTxEntity();
        txEntity.setTxId(signature);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parse_message", parseJson);

            JSONObject additions = new JSONObject();
            additions.put("coin", Coins.APTOS.coinId());
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

    private TxEntity generateAptosTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.APTOS.coinId());
        txEntity.setSignId(getWalletId());
        txEntity.setCoinCode(Coins.APTOS.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    private String getWalletId() {
        if (!TextUtils.isEmpty(origin)) {
            if (origin.equalsIgnoreCase("Petra")) {
                return WatchWallet.PETRA_WALLET_SIGN_ID;
            }
        }
        return watchWallet.getSignId();
    }

    public void parseAptosTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            return;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin) && coin.equals(Coins.APTOS.coinId())) {
                String signature = additions.getJSONObject("addition").getString("signature");
                String rawMessage = additions.getJSONObject("addition").getString("raw_message");
                String parseMessage = additions.getJSONObject("addition").getString("parse_message");
                AptosTxData aptosTxData = new AptosTxData();
                aptosTxData.setSignature(signature);
                aptosTxData.setRawMessage(rawMessage);
                aptosTxData.setParsedMessage(new JSONObject(parseMessage).toString(2));
                aptosTxData.setSignatureUR(txEntity.getSignedHex());
                AptosTx aptosTx = AptosTxParser.parse(parseMessage);
                aptosTxData.setAptosTx(aptosTx);
                aptosTxDataMutableLiveData.postValue(aptosTxData);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }


    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }

}
