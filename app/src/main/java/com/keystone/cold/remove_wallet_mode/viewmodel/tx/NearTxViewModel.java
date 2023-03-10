package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.coins.NEAR.NearImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.cryptocore.NearParser;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.NearTx;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.sparrowwallet.hummingbird.registry.near.NearSignature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NearTxViewModel extends BaseTxViewModel<NearTx> {

    private static final String TAG = NearTxViewModel.class.getSimpleName();

    private String hdPath;
    private String requestId;
    private List<String> txHexList;

    private List<String> signatureList = new ArrayList<>();
    private List<String> txHashList = new ArrayList<>();
    private List<String> formattedJsonList = new ArrayList<>();

    private int transactionNum;

    private Signer signer;
    private String origin;


    public NearTxViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHashList.clear();
            formattedJsonList.clear();
            signatureList.clear();
            origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            txHexList = (List<String>) bundle.getSerializable(BundleKeys.SIGN_DATA_KEY);
            if (txHexList == null) {
                rawFormatTx.postValue(null);
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                return;
            }
            transactionNum = txHexList.size();
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < transactionNum; i++) {
                    String parseResult = NearParser.parse(txHexList.get(i));
                    if (parseResult != null) {
                        String json = getFormattedJson(parseResult);
                        if (json == null) {
                            Log.e(TAG, "have no formatted data");
                            rawFormatTx.postValue(null);
                            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                            return;
                        }
                        Log.e(TAG, String.format("onSuccess is %s", json));
                        NearTx nearTx = NearTx.from(json);
                        observableTransaction.postValue(nearTx);

                        JSONObject jsonObject = new JSONObject(json);
                        txHashList.add(jsonObject.getString("hash"));
                        jsonObject.remove("hash");
                        jsonArray.put(jsonObject);
                        formattedJsonList.add(jsonObject.toString());
                        if (transactionNum - 1 == i) {
                            rawFormatTx.postValue(jsonArray.toString(2));
                        }
                    } else {
                        Log.e(TAG, "parse error");
                        rawFormatTx.postValue(null);
                        observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                rawFormatTx.postValue(null);
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            }

        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signCallBack.startSign();
            for (int i = 0; i < transactionNum; i++) {
                if (!signTransaction(signer, i)) {
                    break;
                }
            }
        });
    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        if (signatureList == null || signatureList.size() == 0) {
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

    public void parseTransactionFromRecord(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            parseNearTxEntity(txEntity);
        });
    }

    private void parseNearTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            observableTransaction.postValue(null);
            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            return;
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
                observableTransaction.postValue(nearTx);
                rawFormatTx.postValue(new JSONObject(parsedMessage).toString(2));
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
            observableTransaction.postValue(null);
            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
        }
    }

    private String getFormattedJson(String nearStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(nearStr);
        return jsonObject.getJSONObject("formatted_json").toString();
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

    private boolean signTransaction(Signer signer, int order) {
        if (txHexList == null || txHexList.size() != txHashList.size()) {
            signCallBack.onFail();
            this.signer = null;
            return false;
        }
        SignTxResult result = new NearImpl().signHex(txHashList.get(order), signer);
        if (result == null) {
            signCallBack.onFail();
            this.signer = null;
            return false;
        } else {
            signatureList.add(result.signaturHex);
            if (transactionNum - 1 == order) {
                signCallBack.onSignTxSuccess();
                this.signer = null;
                insertDB();
            }
            return true;
        }
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
                addition.put("sign_by", NEARAccount.getAccountByPath(hdPath).getCode());
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
            repository.insertTxList(txEntityList);
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
        txEntity.setSignId(getSignId());
        txEntity.setCoinCode(Coins.NEAR.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }

    private String getSignId() {
        return Wallet.SENDER.getSignId();
    }

}
