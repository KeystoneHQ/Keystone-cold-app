package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.arweave.ArweaveTxConfirmFragment.KEY_SALT_LEN;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.cryptocore.ArweaveParser;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.ARPubkeyNotFoundException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ARweaveTxViewModel extends BaseTxViewModel<ArweaveTransaction> {
    private final static String TAG = "ARweaveTxViewModel";
    private final String hdPath = "M/44'/472'";

    private final Application mApplication;

    public ARweaveTxViewModel(@NonNull Application application) {
        super(application);
        this.mApplication = application;
    }

    private String requestId;
    private int saltLen;
    private String messageData;
    private String signature;
    private String origin;

    public void reset() {
        observableException.postValue(null);
        observableTransaction.postValue(null);
        requestId = null;
        saltLen = 0;
        messageData = null;
        signature = null;
        origin = null;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        String signData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
        requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
        saltLen = bundle.getInt(BundleKeys.SALT_LEN_KEY);
        origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
        rawFormatTx.postValue(signData);

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String json = new String(Hex.decode(signData), StandardCharsets.UTF_8);
                JSONObject object = new JSONObject(json);
                String owner = object.optString("owner");
                String pubkey = ArweaveViewModel.getARPublicKey();
                if (pubkey == null) {
                    observableException.postValue(ARPubkeyNotFoundException.newInstance());
                    return;
                }
                String myOwner = ArweaveViewModel.formatHex(Hex.decode(pubkey));
                if (owner.equals("")) {
                    object.put("owner", myOwner);
                } else {
                    if (!owner.equalsIgnoreCase(myOwner)) {
                        observableException.postValue(new InvalidTransactionException(mApplication.getString(R.string.ar_pubkey_not_match), "invalid transaction, owner not match"));
                        return;
                    }
                }
                String jsonString = ArweaveParser.parse(Hex.toHexString(object.toString().getBytes(StandardCharsets.UTF_8)));
                JSONObject rccObject = new JSONObject(jsonString);
                String status = rccObject.getString("status");
                if (status.equals("success")) {
                    observableTransaction.postValue(ArweaveTransaction.fromRCC(rccObject));
                } else {
                    observableException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, transaction parsed failed"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                observableException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, json error"));
            }
        });
    }

    public void parseExistingTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            try {
                JSONObject signedRawTx = new JSONObject(txEntity.getSignedHex());
                rawFormatTx.postValue(txEntity.getSignedHex());
                JSONObject addition = new JSONObject(txEntity.getAddition());
                requestId = addition.getString("requestId");
                signature = addition.getString("signature");
                ArweaveTransaction transaction = ArweaveTransaction.fromJSON(signedRawTx);
                transaction.setSignatureUR(this.getSignatureUR());
                observableTransaction.postValue(transaction);
            } catch (JSONException e) {
                e.printStackTrace();
                observableException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, json error"));
            }
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                messageData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                requestId = bundle.getString(REQUEST_ID);
                saltLen = bundle.getInt(KEY_SALT_LEN);
                String fromAddress = ArweaveViewModel.getARAddress();
                JSONObject object = new JSONObject();
                object.put("hdPath", hdPath);
                object.put("requestId", requestId);
                object.put("data", messageData);
                object.put("fromAddress", fromAddress);
                observableObject.postValue(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return observableObject;
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            signState.postValue(STATE_SIGNING);
            ArweaveTransaction arweaveTransaction = observableTransaction.getValue();
            if (arweaveTransaction == null) return;
            RustSigner signer = initSigner();
            if (signer == null) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            signature = signer.signRSA(arweaveTransaction.getSignatureData(), saltLen);
            if (signature == null) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            try {
                String txId = ArweaveViewModel.formatHex(Util.sha256(Hex.decode(signature)));
                insertDB(signature, txId, arweaveTransaction);
                signState.postValue(STATE_SIGN_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                signState.postValue(STATE_SIGN_FAIL);
            } finally {
                new ClearTokenCallable().call();
            }
        });
    }

    private TxEntity generateTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.AR.coinId());
        //update origin
        txEntity.setSignId(WatchWallet.ARWEAVE_SIGN_ID);
        txEntity.setCoinCode(Coins.AR.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        return txEntity;
    }

    private void insertDB(String signature, String txId, ArweaveTransaction tx) throws JSONException {
        TxEntity txEntity = generateTxEntity();
        txEntity.setTxId(txId);
        JSONObject rawTx = tx.getRawTx();
        rawTx.put("signature", ArweaveViewModel.formatHex(Hex.decode(signature)));
        rawTx.put("id", txId);
        txEntity.setSignedHex(rawTx.toString());
        txEntity.setAddition(
                new JSONObject().put("rawTx", tx.getRawTx())
                        .put("requestId", requestId)
                        .put("signature", signature)
                        .toString()
        );
        repository.insertTx(txEntity);
    }

    private RustSigner initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new RustSigner(hdPath.toLowerCase(), authToken);
    }

    @Override
    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            signState.postValue(STATE_SIGNING);
            RustSigner signer = initSigner();
            if (signer == null) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            signature = signer.signRSA(messageData, saltLen);
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        });
    }

    @Override
    public String getSignatureUR() {
        byte[] signature = Hex.decode(this.signature);
        UUID uuid = UUID.fromString(this.requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        ArweaveSignature arweaveSignature = new ArweaveSignature(signature, requestId);
        return arweaveSignature.toUR().toString();
    }
}
