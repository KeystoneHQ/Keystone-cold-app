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

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.NEAR.NearImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;

import com.keystone.cold.cryptocore.NearParser;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.fragment.main.near.model.NearTx;
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
    private List<String> txHashList = new ArrayList<>();
    private List<String> formattedJsonList = new ArrayList<>();


    private int transactionNum = 0;

    private final MutableLiveData<JSONArray> parseMessageJsonLiveData;
    private final MutableLiveData<NearTx> nearTxLiveData;


    public NearTxViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        coinCode = "NEAR";
        parseMessageJsonLiveData = new MutableLiveData<>();
        nearTxLiveData = new MutableLiveData<>();
    }


    public MutableLiveData<NearTx> getNearTxLiveData() {
        return nearTxLiveData;
    }

    public MutableLiveData<JSONArray> getParseMessageJsonLiveData() {
        return parseMessageJsonLiveData;
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
            signer = null;
        }

        @Override
        public void onSignTxSuccess(String signatureHex, int order) {
            signatureList.add(signatureHex);
            if (transactionNum - 1 == order) {
                signState.postValue(STATE_SIGN_SUCCESS);
                new ClearTokenCallable().call();
                signer = null;
                insertDB();
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
            txHashList.clear();
            formattedJsonList.clear();
            signatureList.clear();
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);
            transactionNum = txHexList.size();

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < transactionNum; i++) {

                String parseResult = NearParser.parse(txHexList.get(i));
                if (parseResult != null) {
                    String json = getFormattedJson(parseResult);
                    if (json == null) {
                        Log.e(TAG, "have no formatted data");
                        parseMessageJsonLiveData.postValue(null);
                        return;
                    }
                    Log.e(TAG, String.format("onSuccess is %s", json));
                    NearTx nearTx = NearTx.from(json);
                    nearTxLiveData.postValue(nearTx);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(json);
                        txHashList.add(jsonObject.getString("hash"));
                        jsonObject.remove("hash");
                        jsonArray.put(jsonObject);
                        formattedJsonList.add(jsonObject.toString());
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                    if (transactionNum - 1 == i) {
                        parseMessageJsonLiveData.postValue(jsonArray);
                    }
                } else {
                    Log.e(TAG, "parse error");
                    parseMessageJsonLiveData.postValue(null);
                }
            }

        });
    }

    private String getFormattedJson(String nearStr) {
        try {
            JSONObject jsonObject = new JSONObject(nearStr);
            return jsonObject.getJSONObject("formatted_json").toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
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

    private void insertDB() {
        if (signatureList.size() != txHexList.size()
                || signatureList.size() != txHashList.size()
                || signatureList.size() != formattedJsonList.size()) {
            return;
        }
        String signBatchId = getSignBatchId();
        List<TxEntity> txEntityList = new ArrayList<>();
        for (int i = 0; i < signatureList.size(); i++) {
            String signature = signatureList.get(i);
            String rawMessage = txHexList.get(i);
            String parsedMessage = formattedJsonList.get(i);
            String hash = txHashList.get(i);
            TxEntity txEntity = generateNearTxEntity();
            txEntity.setTxId(hash);
            String additionsString = null;
            try {
                JSONObject addition = new JSONObject();
                addition.put("signature", signature);
                addition.put("raw_message", rawMessage);
                addition.put("parsed_message", parsedMessage);
                addition.put("sign_by", Utilities.getCurrentNearAccount(getApplication()));
                addition.put("sign_batch_info", getSignTogether(signBatchId, i));
                JSONObject additions = new JSONObject();
                additions.put("coin", Coins.NEAR.coinId());
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
            }
            txEntityList.add(txEntity);
        }

        if (!txEntityList.isEmpty()) {
            mRepository.insertTxList(txEntityList);
        }

    }

    private String getSignBatchId() {
        StringBuilder sb = new StringBuilder();
        for (String hash : txHashList) {
            sb.append(hash);
        }
        return Util.sha3String(sb.toString());
    }

    private JSONObject getSignTogether(String signBatchId, int order) throws JSONException {
        JSONObject signBatchInfo = new JSONObject();
        signBatchInfo.put("batch_id", signBatchId);
        signBatchInfo.put("order", order);
        return signBatchInfo;
    }

    private TxEntity generateNearTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.NEAR.coinId());
        txEntity.setSignId(watchWallet.getSignId());
        txEntity.setCoinCode(Coins.NEAR.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    public void parseNearTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            nearTxLiveData.postValue(null);
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin) && coin.equals(Coins.NEAR.coinId())) {
                String parsedMessage = additions.getJSONObject("addition").getString("parsed_message");
                NearTx nearTx = NearTx.from(parsedMessage);
                nearTx.setRawData(parsedMessage);
                nearTx.setUr(txEntity.getSignedHex());
                nearTxLiveData.postValue(nearTx);
                return;
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        nearTxLiveData.postValue(null);
    }


    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex, int order);

        void onSignMsgSuccess(String signatureHex);
    }
}
