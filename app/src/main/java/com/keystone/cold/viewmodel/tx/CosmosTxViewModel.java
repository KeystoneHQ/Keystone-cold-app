package com.keystone.cold.viewmodel.tx;

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
import com.keystone.coinlib.coins.APTOS.AptosImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.coins.cosmos.CosmosImpl;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.ui.fragment.main.cosmos.model.CosmosTx;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignature;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;


public class CosmosTxViewModel extends Base {


    private String txHex;
    private String messageData;
    private String hdPath;
    private String dataType;

    private String requestId;
    private String signature;

    private String xPub;

    private final MutableLiveData<JSONObject> parseMessageJsonLiveData;



    private final MutableLiveData<CosmosTx> cosmosTxLiveData;


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
    }

    public LiveData<JSONObject> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    public MutableLiveData<CosmosTx> getCosmosTxLiveData() {
        return cosmosTxLiveData;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            dataType = bundle.getString(DATA_TYPE);

            if ("sign-type-amino".equals(dataType)) {
                parseAminoTx();
            } else if ("sign-type-direct".equals(dataType)){
                parseDirectTx();
            }
            xPub = getXpubByPath(hdPath);

        });
    }

    private void parseDirectTx() {

    }

    private void parseAminoTx() {
        String aminoMessage = new String(Hex.decode(txHex));
        CosmosTx cosmosTx = CosmosTx.from(aminoMessage);
        cosmosTxLiveData.postValue(cosmosTx);

        try {
            parseMessageJsonLiveData.postValue(new JSONObject(aminoMessage));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

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
        SignTxResult result = new CosmosImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.signaturHex);
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
        byte[] publicKey = null;
        if (xPub != null) {
            publicKey = getPublicKey(xPub);
        }
        CosmosSignature cosmosSignature = new CosmosSignature(signatureByte, requestId, publicKey);
        return cosmosSignature.toUR().toString();
    }

    private byte[] getPublicKey(String xPub) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        return extendedPublicKey.getKey();
    }

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }
}