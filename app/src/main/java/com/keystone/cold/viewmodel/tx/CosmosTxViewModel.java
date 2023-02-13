package com.keystone.cold.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.CUSTOM_CHAIN_IDENTIFIER;
import static com.keystone.cold.ui.fragment.main.AssetFragment.DATA_TYPE;
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
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.coins.cosmos.CosmosImpl;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.cryptocore.CosmosParser;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.ui.fragment.main.cosmos.model.CosmosTx;
import com.keystone.cold.ui.fragment.main.cosmos.model.CosmosTxData;
import com.keystone.cold.ui.fragment.main.cosmos.model.msg.Msg;
import com.keystone.cold.ui.fragment.main.cosmos.model.msg.MsgSignData;
import com.keystone.cold.util.HashUtil;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignRequest;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignature;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignRequest;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;


public class CosmosTxViewModel extends Base {


    private String txHex;
    private String messageData;
    private String hdPath;
    private String dataType;

    private String requestId;
    private String signature;

    private String xPub;
    private String chainId;
    private String parseJson;

    private final MutableLiveData<JSONObject> parseMessageJsonLiveData;


    private final MutableLiveData<CosmosTx> cosmosTxLiveData;
    private final MutableLiveData<CosmosTxData> cosmosTxDataMutableLiveData;

    private long evmChainId;

    private enum SignMode {
        COSMOS("cosmos"), EVM("evm");

        private String signMode;

        SignMode(String signMode) {
            this.signMode = signMode;
        }

        public String getSignMode() {
            return signMode;
        }
    }
    private SignMode signMode = SignMode.COSMOS;


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


    public CosmosTxViewModel(@NonNull Application application) {
        super(application);
        parseMessageJsonLiveData = new MutableLiveData<>();
        cosmosTxLiveData = new MutableLiveData<>();
        cosmosTxDataMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<JSONObject> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    public MutableLiveData<CosmosTx> getCosmosTxLiveData() {
        return cosmosTxLiveData;
    }

    public MutableLiveData<CosmosTxData> getCosmosTxDataMutableLiveData() {
        return cosmosTxDataMutableLiveData;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            dataType = bundle.getString(DATA_TYPE);
            evmChainId = bundle.getLong(CUSTOM_CHAIN_IDENTIFIER);
            try {
                if (CosmosSignRequest.DataType.AMINO.getType().equals(dataType)) {
                    parseAminoTx();
                    signMode = SignMode.COSMOS;
                } else if (CosmosSignRequest.DataType.DIRECT.getType().equals(dataType)) {
                    parseDirectTx();
                    signMode = SignMode.COSMOS;
                } else if (EvmSignRequest.DataType.AMINO_TRANSACTION.getType().equals(dataType)) {
                    parseAminoTx();
                    signMode = SignMode.EVM;
                } else if (EvmSignRequest.DataType.DIRECT_TRANSACTION.getType().equals(dataType)) {
                    parseDirectTx();
                    signMode = SignMode.EVM;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            xPub = getXpubByPath(hdPath);
        });
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
                parseMessageJsonLiveData.postValue(new JSONObject(parseJson));
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
            parseMessageJsonLiveData.postValue(new JSONObject(parseJson));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }


    public MutableLiveData<JSONObject> parseRawMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                hdPath = bundle.getString(HD_PATH);
                requestId = bundle.getString(REQUEST_ID);
                messageData = bundle.getString(SIGN_DATA);
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
                parseTxException.postValue(e);
            }
            xPub = getXpubByPath(hdPath);
        });
        return observableObject;
    }


    private String getXpubByPath(String path) {
        DataRepository repository = MainApplication.getApplication().getRepository();
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

    private Signer initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new RustSigner(hdPath.toLowerCase(), authToken);
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signTransaction(signer);
        });
    }

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        String signature = null;
        switch (signMode) {
            case COSMOS:
                SignTxResult result = new CosmosImpl().signHex(txHex, signer);
                signature = result.signaturHex;
                break;
            case EVM:
                signature = new EthImpl(evmChainId).directSign(txHex, signer);
                break;
        }
        if (signature == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(signature);
        }
    }

    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signMessage(signer);
        });
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new CosmosImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignMsgSuccess(result);
        }
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
        switch (signMode) {
            case COSMOS:
                byte[] publicKey = null;
                if (xPub != null) {
                    publicKey = getPublicKey(xPub);
                }
                CosmosSignature cosmosSignature = new CosmosSignature(signatureByte, requestId, publicKey);
                return cosmosSignature.toUR().toString();
            case EVM:
                EvmSignature evmSignature = new EvmSignature(signatureByte, requestId);
                return evmSignature.toUR().toString();
        }
       return "";
    }

    private byte[] getPublicKey(String xPub) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        return extendedPublicKey.getKey();
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
            addition.put("sign_mode", signMode.getSignMode());
            addition.put("chain_id", chainId);
            JSONObject additions = new JSONObject();
            additions.put("coin", getCosmosCoinId(chainId));
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

    private TxEntity generateCosmosTxEntity() {
        TxEntity txEntity = new TxEntity();
        String coinId = getCosmosCoinId(chainId);
        txEntity.setCoinId(coinId);
        txEntity.setSignId(watchWallet.getSignId());
        String coinCode = getCosmosCoinCode(chainId);
        txEntity.setCoinCode(coinCode);
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    public void parseCosmosTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            return;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin)) {
                String signature = additions.getJSONObject("addition").getString("signature");
                String rawMessage = additions.getJSONObject("addition").getString("raw_message");
                String parseMessage = additions.getJSONObject("addition").getString("parse_message");
                CosmosTxData cosmosTxData = new CosmosTxData();
                cosmosTxData.setSignature(signature);
                cosmosTxData.setRawMessage(rawMessage);
                cosmosTxData.setParsedMessage(new JSONObject(parseMessage).toString(2));
                cosmosTxData.setSignatureUR(txEntity.getSignedHex());
                CosmosTx cosmosTx = CosmosTx.from(parseMessage);
                cosmosTxData.setCosmosTx(cosmosTx);
                cosmosTxDataMutableLiveData.postValue(cosmosTxData);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    public String getCosmosCoinId(String chainId) {
        return Coins.getCosmosCoinId(chainId);
    }

    public String getCosmosCoinCode(String chainId) {
        return Coins.getCosmosCoinCode(chainId);
    }

    public String getCosmosCoinCode() {
        return Coins.getCosmosCoinCode(chainId);
    }

    public String getCosmosCoinName(String chainId) {
        if (!TextUtils.isEmpty(chainId) && chainId.contains("9000")){
            return "Evmos Testnet";
        }
        return Coins.coinNameFromCoinCode(getCosmosCoinCode(chainId));
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

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }
}