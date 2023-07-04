package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.coins.SUI.SuiImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.cryptocore.SuiParser;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AptosAddressGenerator;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.main.sui.model.SuiTx;
import com.sparrowwallet.hummingbird.registry.sui.SuiSignature;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class SuiTxViewModel extends BaseTxViewModel<SuiTx> {

    private static final String TAG = "SuiTxViewModel";

    private String requestId;
    private String txHex;
    private String messageData;
    private String messageHex;
    private String hdPath;

    private String xPub;

    private String parseJson;
    private String signature;

    private String origin;

    public SuiTxViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
            String parseResult = SuiParser.parseTransaction(txHex.substring(6));
            Log.i(TAG, "raw is " + parseResult);
            JSONObject jsonObject;
            if (parseResult != null) {
                try {
                    jsonObject = new JSONObject(parseResult);
                    SuiTx suiTx = SuiTx.fromRaw(jsonObject);
                    observableTransaction.postValue(suiTx);
                    Log.i(TAG, "suiTx is " + suiTx);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    jsonObject = new JSONObject();
                    observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                }
                parseJson = jsonObject.toString();
                try {
                    rawFormatTx.postValue(jsonObject.toString(2));
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
                xPub = getXpubByPath(hdPath);
            } else {
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            messageData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            messageHex = "";
            String fromAddress = getFromAddress(hdPath);
            String parseResult = SuiParser.parseMessage(messageData.substring(6));
            Log.i(TAG, "raw is " + parseResult);
            try {
                JSONObject parseJsonObject = new JSONObject(parseResult);
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("requestId", requestId);
                JSONArray messageArray = parseJsonObject.getJSONArray("message");
                byte[] messageBytes = new byte[messageArray.length()];
                for (int i = 0; i < messageArray.length(); i++) {
                    messageBytes[i] = (byte) messageArray.getInt(i);
                }
                messageHex = Hex.toHexString(messageBytes);
                String message = new String(messageBytes, StandardCharsets.UTF_8);
                object.put("data", message);
                object.put("fromAddress", fromAddress);
                observableObject.postValue(object);
                xPub = getXpubByPath(hdPath);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid message"));
            }
        });
        return observableObject;
    }

    public void parseTransactionFromRecord(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            parseSuiTxEntity(txEntity);
        });
    }

    private void parseSuiTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            return;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin) && coin.equals(Coins.SUI.coinId())) {
                String parseMessage = additions.getJSONObject("addition").getString("parse_message");
                JSONObject jsonObject = new JSONObject(parseMessage);
                SuiTx suiTx = SuiTx.fromRaw(jsonObject);
                if (suiTx != null) {
                    suiTx.setSignatureUR(txEntity.getSignedHex());
                    observableTransaction.postValue(suiTx);
                    rawFormatTx.postValue(new JSONObject(parseMessage).toString(2));
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
        }
    }


    public String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            return repository.loadAddressBypath(path).getAddressString();
        } catch (InvalidAccountException e) {
            e.printStackTrace();
            observableException.postValue(e);
        }
        return "";
    }


    private void ensureAddressExist(String path) throws InvalidAccountException {
        path = path.toUpperCase();
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setPath(path);
        AddressEntity address = repository.loadAddressBypath(path);
        if (address == null) {
            updateAccountDb(getAddressIndex(path));
        }
    }


    private int getAddressIndex(String hdPath) {
        int index = 0;
        hdPath = hdPath.toUpperCase();
        if (!hdPath.startsWith("M/")) {
            hdPath = "M/" + hdPath;
        }
        hdPath = hdPath.replace("'", "");
        String[] strings = hdPath.split("/");
        if (strings.length == 6) {
            index = Integer.parseInt(strings[4]);
        }
        return index;
    }

    protected void updateAccountDb(int addressIndex) throws InvalidAccountException {
        CoinEntity coin = repository.loadCoinEntityByCoinCode(Coins.APTOS.coinCode());
        List<AccountEntity> accounts = repository.loadAccountsForCoin(coin);
        if (accounts == null || accounts.size() == 0) {
            throw new InvalidAccountException(getApplication().getString(R.string.incorrect_tx_data), "not have match account");
        }
        int currentAddressCount = accounts.get(0).getAddressLength();
        int count = addressIndex + 1 - currentAddressCount;
        if (count > 0) {
            new AptosAddressGenerator().generateAddress(count);
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


    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signTransaction(signer);
        });
    }

    @Override
    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signMessage(signer);
        });
    }


    @Override
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
        SuiSignature suiSignature = new SuiSignature(signatureByte, requestId, publicKey);
        return suiSignature.toUR().toString();
    }

    private byte[] getPublicKey(String xPub) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        byte[] key = extendedPublicKey.getKey();
        byte[] publicKey = new byte[32];
        System.arraycopy(key, 1, publicKey, 0, 32);
        return publicKey;
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

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        String result = new SuiImpl().signMessage(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result;
            signCallBack.onSignTxSuccess();
            insertToDB(result, txHex, parseJson);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new SuiImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result;
            signCallBack.onSignMsgSuccess();
        }
    }


    private void insertToDB(String signature, String rawMessage, String parseJson) {
        TxEntity txEntity = generateSuiTxEntity();
        txEntity.setTxId(signature);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parse_message", parseJson);

            JSONObject additions = new JSONObject();
            additions.put("coin", Coins.SUI.coinId());
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
            repository.insertTx(txEntity);
        }
    }

    private TxEntity generateSuiTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.SUI.coinId());
        txEntity.setSignId(getSignId());
        txEntity.setCoinCode(Coins.SUI.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }


    // Current only fewcha support aptos.
    // If add other wallets in the future, need to rewrite
    private String getSignId() {
        if (TextUtils.isEmpty(origin)) {
            return Wallet.FEWCHA.getSignId();
        }
        if ("Suiet".equalsIgnoreCase(origin)) {
            return Wallet.SUIET.getSignId();
        }
        return Wallet.UNKNOWNWALLET.getSignId();
    }
}
