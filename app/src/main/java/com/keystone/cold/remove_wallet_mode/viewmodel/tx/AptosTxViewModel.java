package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.coins.APT.AptImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.cryptocore.AptosParser;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AptosAddressGenerator;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTx;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTxParser;
import com.keystone.cold.util.AptosTransactionHelper;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class AptosTxViewModel extends BaseTxViewModel {

    private static final String TAG = "AptosTxViewModel";

    private String requestId;
    private String txHex;
    private String messageData;
    private String hdPath;

    private String xPub;

    private String parseJson;
    private String signature;


    private final MutableLiveData<AptosTx> aptosTxLiveData;


    public AptosTxViewModel(@NonNull Application application) {
        super(application);
        aptosTxLiveData = new MutableLiveData<>();
    }


    public LiveData<AptosTx> getAptosTxLiveData() {
        return aptosTxLiveData;
    }


    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
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
            parseJson = jsonObject.toString();
            try {
                rawFormatTx.postValue(jsonObject.toString(2));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
            xPub = getXpubByPath(hdPath);
        });
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
            }
        });
        return observableObject;
    }

    public String getFromAddress(String path) {
        try {
            ensureAddressExist(path);
            return repository.loadAddressBypath(path).getAddressString();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void ensureAddressExist(String path) throws InvalidTransactionException {
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
        CoinEntity coin = repository.loadCoinEntityByCoinCode(Coins.APTOS.coinCode());
        List<AccountEntity> accounts= repository.loadAccountsForCoin(coin);
        if (accounts == null || accounts.size() == 0) {
            throw new InvalidTransactionException("not have match account");
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
        AptosSignature aptosSignature = new AptosSignature(signatureByte, requestId, publicKey);
        return aptosSignature.toUR().toString();
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
        SignTxResult result = new AptImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result.signaturHex;
            signCallBack.onSignTxSuccess();
            insertToDB(result.signaturHex, txHex, parseJson);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new AptImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result;
            signCallBack.onSignMsgSuccess();
        }
    }


    private void insertToDB(String signature, String rawMessage, String parseJson) {
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
            repository.insertTx(txEntity);
        }
    }

    private TxEntity generateAptosTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.APTOS.coinId());
        //todo 根据ur中的origin字段进行匹配设置，此处暂时忽略，先跑通整体流程
        txEntity.setSignId("");
        txEntity.setCoinCode(Coins.APTOS.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }
}
