package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.coins.cosmos.CosmosImpl;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.cryptocore.CosmosParser;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.CosmosTx;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.Msg;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgSignData;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.util.HashUtil;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignature;


import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public class CosmosTxViewModel extends BaseTxViewModel {

    private static final String TAG = CosmosTxViewModel.class.getSimpleName();
    private String txHex;
    private String messageData;
    private String hdPath;
    private String dataType;
    private String origin;

    private String requestId;

    private String xPub;
    private String chainId;
    private String parseJson;

    private String signature;


    private final MutableLiveData<CosmosTx> cosmosTxLiveData;


    public CosmosTxViewModel(@NonNull Application application) {
        super(application);
        cosmosTxLiveData = new MutableLiveData<>();
    }


    public LiveData<CosmosTx> getCosmosTxLiveData() {
        return cosmosTxLiveData;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            dataType = bundle.getString(BundleKeys.DATA_TYPE_KEY);
            origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
            try {
                if ("sign-type-amino".equals(dataType)) {
                    parseAminoTx();
                } else if ("sign-type-direct".equals(dataType)) {
                    parseDirectTx();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            xPub = getXpubByPath(hdPath);
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                messageData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
                requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
                String signer = null;
                String data = null;
                String aminoMessage = new String(Hex.decode(messageData));
                CosmosTx cosmosTx = CosmosTx.from(aminoMessage);
                if (cosmosTx != null) {
                    chainId = cosmosTx.getChainId();
                    if (cosmosTx.getMsgs() != null && cosmosTx.getMsgs().size() != 0) {
                        for (Msg msg : cosmosTx.getMsgs()) {
                            if (msg instanceof MsgSignData) {
                                signer = ((MsgSignData) msg).getSigner();
                                data = ((MsgSignData) msg).getData();
                                break;
                            }
                        }
                    }
                }
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("requestId", requestId);
                object.put("data", data);
                object.put("chainId", chainId);
                object.put("signer", signer);
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
                observableObject.postValue(null);
            }
            xPub = getXpubByPath(hdPath);
        });
        return observableObject;
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
        CosmosSignature cosmosSignature = new CosmosSignature(signatureByte, requestId, publicKey);
        return cosmosSignature.toUR().toString();
    }


    public void parseTransactionFromRecord(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            parseCosmosTxEntity(txEntity);
        });
    }

    private void parseCosmosTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            return;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin)) {
                String parseMessage = additions.getJSONObject("addition").getString("parse_message");
                rawFormatTx.postValue(new JSONObject(parseMessage).toString(2));
                CosmosTx cosmosTx = CosmosTx.from(parseMessage);
                if (cosmosTx != null) {
                    cosmosTx.setUr(txEntity.getSignedHex());
                }
                cosmosTxLiveData.postValue(cosmosTx);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    public String getCosmosCoinCode() {
        return Coins.getCosmosCoinCode(chainId);
    }

    private void parseDirectTx() {
        String parseResult = CosmosParser.parse(txHex);
        if (parseResult != null) {
            parseJson = CosmosTx.transformDirectToAmino(parseResult);
            CosmosTx cosmosTx = CosmosTx.from(parseJson);
            cosmosTxLiveData.postValue(cosmosTx);
            if (cosmosTx != null) {
                chainId = cosmosTx.getChainId();
            }
            try {
                rawFormatTx.postValue(new JSONObject(parseJson).toString(2));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void parseAminoTx() {
        parseJson = new String(Hex.decode(txHex));
        CosmosTx cosmosTx = CosmosTx.from(parseJson);
        cosmosTxLiveData.postValue(cosmosTx);
        if (cosmosTx != null) {
            chainId = cosmosTx.getChainId();
        }
        try {
            rawFormatTx.postValue(new JSONObject(parseJson).toString(2));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    private String getXpubByPath(String path) {
        AddressEntity addressEntity;
        if (isEvmosPath(path)) {
            addressEntity = repository.loadAddressByPathAndCoinId(path, Coins.EVMOS.coinId());
        } else {
            addressEntity = repository.loadAddressBypath(path);
        }
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

    private boolean isEvmosPath(String path) {
        //M/44'/60'/0'/0/0
        if (null != path) {
            String[] segs = path.split("/");
            String coinType = segs[2].replace("'", "");
            return coinType.equals(String.valueOf(Coins.EVMOS.coinIndex()));
        }
        return false;
    }

    private Signer initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new RustSigner(hdPath.toLowerCase(), authToken);
    }

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new CosmosImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess();
            signature = result.signaturHex;
            insertDB(signature, txHex, parseJson);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new CosmosImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignMsgSuccess();
            signature = result;
        }
    }

    private void insertDB(String signature, String rawMessage, String parseJson) {
        TxEntity txEntity = generateCosmosTxEntity();
        String txId = Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(Hex.decode(txHex))));
        txEntity.setTxId(txId);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parse_message", parseJson);

            JSONObject additions = new JSONObject();
            additions.put("coin", getCosmosCoinId());
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

    private TxEntity generateCosmosTxEntity() {
        TxEntity txEntity = new TxEntity();
        String coinId = getCosmosCoinId();
        txEntity.setCoinId(coinId);
        txEntity.setSignId(getSignId());
        String coinCode = getCosmosCoinCode();
        txEntity.setCoinCode(coinCode);
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    private String getSignId() {
        return Wallet.KEPLR.getSignId();
    }

    public String getCosmosCoinId() {
        return Coins.getCosmosCoinId(chainId);
    }

    private byte[] getPublicKey(String xPub) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        return extendedPublicKey.getKey();
    }

}
