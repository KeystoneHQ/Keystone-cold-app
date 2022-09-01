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

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.coinlib.coins.APTOS.AptosImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.encryption.ChipSigner;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;

import java.util.UUID;

public class AptosViewModel extends Base {

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private String txHex;
    private String messageData;
    private String hdPath;

    private String requestId;
    private String signature;

    private String xPub;

    private final MutableLiveData<JSONObject> parseMessageJsonLiveData;


    public AptosViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        coinCode = "APTOS";
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
        SignTxResult result = new AptosImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.signaturHex);
        }
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new AptosImpl().signMessage(messageData, signer);
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

    public MutableLiveData<JSONObject> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("raw", txHex);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
            parseMessageJsonLiveData.postValue(jsonObject);
            xPub = getXpubByPath(hdPath);
        });
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

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }

}
