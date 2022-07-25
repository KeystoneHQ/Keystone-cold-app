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

import com.keystone.coinlib.coins.NEAR.NearImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;

import com.keystone.cold.encryption.ChipSigner;
import com.sparrowwallet.hummingbird.registry.near.NearSignature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class NearTxViewModel extends Base {

    private static final String TAG = NearTxViewModel.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private String hdPath;
    private String requestId;
    private List<String> txHexList;

    private Signer signer;
    private List<String> signatureList = new ArrayList<>();
    private List<String> txHashList;


    private int transactionNum = 0;

    private final MutableLiveData<JSONArray> parseMessageJsonLiveData;


    public NearTxViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        coinCode = "NEAR";
        parseMessageJsonLiveData = new MutableLiveData<>();

    }


    public MutableLiveData<JSONArray> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
    }

    private final SignCallBack signCallBack = new SignCallBack() {
        @Override
        public void startSign() {
            signState.postValue(STATE_SIGNING);
            signatureList.clear();
        }

        @Override
        public void onFail() {
            signState.postValue(STATE_SIGN_FAIL);
            new ClearTokenCallable().call();
            signer = null;
        }

        @Override
        public void onSignTxSuccess(String signatureHex, int order) {
            signatureList.add(signatureHex);
            if (transactionNum - 1 == order) {
                signState.postValue(STATE_SIGN_SUCCESS);
                new ClearTokenCallable().call();
                signer = null;
            }
        }

        @Override
        public void onSignMsgSuccess(String signatureHex) {
            signatureList.add(signatureHex);
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
            signer = null;
        }
    };

    @SuppressWarnings("unchecked")
    public void parseTxData(Bundle bundle) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            txHexList = (List<String>) bundle.getSerializable(SIGN_DATA);
            txHashList = new ArrayList<>();
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            transactionNum = txHexList.size();

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < transactionNum; i++) {
                int finalI = i;
                NearImpl.parseMessage(txHexList.get(i), new NearImpl.ParseMessageCallback() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e(TAG, String.format("onSuccess is %s", json));
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(json);
                            txHashList.add(jsonObject.getString("hash"));
                            jsonObject.remove("hash");
                            jsonArray.put(jsonObject);
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                        }
                        if (transactionNum - 1 == finalI) {
                            parseMessageJsonLiveData.postValue(jsonArray);
                        }
                    }

                    @Override
                    public void onFailed(String error) {
                        Log.e(TAG, String.format("parse error is %s", error));
                        parseMessageJsonLiveData.postValue(null);

                    }
                });
            }

        });
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signCallBack.startSign();
            for (int i = 0; i < transactionNum; i++) {
                signTransaction(signer, i);
            }
        });
    }

    private Signer initSigner() {
        if (signer == null) {
            String authToken = getAuthToken();
            if (TextUtils.isEmpty(authToken)) {
                Log.w(TAG, "authToken null");
                return null;
            }
            signer = new ChipSigner(hdPath.toLowerCase(), authToken);
        }
        return signer;
    }


    private void signTransaction(Signer signer, int order) {
        if (txHexList == null || txHexList.size() != txHashList.size()) {
            signCallBack.onFail();
            return;
        }
        SignTxResult result = new NearImpl().signHex(txHashList.get(order), signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signCallBack.onSignTxSuccess(result.signaturHex, order);
        }
    }

    public String getSignatureUR() {
        if (signatureList == null && signatureList.size() == 0) {
            return "";
        }

        List<byte[]> signatureBytes = new ArrayList<>(signatureList.size());
        for (String signature : signatureList) {
            byte[] signatureByte = Hex.decode(signature);
            signatureBytes.add(signatureByte);
        }

        UUID uuid = UUID.fromString(requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        NearSignature nearSignature = new NearSignature(signatureBytes, requestId);
        return nearSignature.toUR().toString();
    }


    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex, int order);

        void onSignMsgSuccess(String signatureHex);
    }
}
