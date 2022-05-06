package com.keystone.cold.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.fragment.main.solana.model.SolTxData;
import com.keystone.cold.viewmodel.callback.ParseCallback;
import com.sparrowwallet.hummingbird.registry.solana.SolSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SolTxViewModel extends Base {
    private String txHex;
    private String messageData;
    private String hdPath;
    private int chainId;

    private String requestId;
    private String signature;

    private String parsedMessage;

    public SolTxViewModel(@NonNull Application application) {
        super(application);
        coinCode = "SOL";
    }

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess(String signatureHex);

        void onSignMsgSuccess(String signatureHex);
    }

    private SignCallBack signCallBack = new SignCallBack() {
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
            insertDB(signature,txHex,parsedMessage);
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

    public void parseTxData(Bundle bundle, final ParseCallback parseCallback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);

            SolImpl.parseMessage(txHex, new SolImpl.ParseMessageCallback() {
                @Override
                public void onSuccess(String json) {
                    parsedMessage = json;
                    AppExecutors.getInstance().mainThread().execute(() -> parseCallback.OnSuccess(json));
                }

                @Override
                public void onFailed() {
                    AppExecutors.getInstance().mainThread().execute(() -> parseCallback.onFailed());
                }
            });
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

    public String getSignatureUR(){
        if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(requestId)) {
            return "";
        }
        byte[] signatureByte = Hex.decode(signature);
        UUID uuid = UUID.fromString(requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        SolSignature solSignature = new SolSignature(signatureByte,requestId);
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

    private void insertDB(String signature, String rawMessage, String parsedMessage){
        TxEntity txEntity = generateSolTxEntity();
        txEntity.setTxId(signature);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parsed_message", parsedMessage);
            addition.put("sign_by",Utilities.getCurrentSolAccount(getApplication()));
            JSONObject additions = new JSONObject();
            additions.put("coin", Coins.SOL.coinId());
            additions.put("addition", addition);
            JSONObject root = new JSONObject();
            root.put("additions", additions);
            additionsString = root.toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        if (!TextUtils.isEmpty(additionsString)){
            //addition结构详见 com.keystone.cold.db.entity.TxEntity addition字段
            txEntity.setAddition(additionsString);
            mRepository.insertTx(txEntity);
        }
    }

    private TxEntity generateSolTxEntity(){
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.SOL.coinId());
        txEntity.setSignId(watchWallet.getSignId());
        txEntity.setCoinCode(Coins.SOL.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }
}
