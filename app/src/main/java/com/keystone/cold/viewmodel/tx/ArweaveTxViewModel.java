package com.keystone.cold.viewmodel.tx;

import static com.keystone.cold.viewmodel.tx.SignState.STATE_SIGNING;
import static com.keystone.cold.viewmodel.tx.SignState.STATE_SIGN_FAIL;
import static com.keystone.cold.viewmodel.tx.SignState.STATE_SIGN_SUCCESS;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.cryptocore.ArweaveParser;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.InvalidStateException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;

public class ArweaveTxViewModel extends AndroidViewModel {
    private final String TAG = "ArweaveViewModel";
    private final Application mApplication;
    private final DataRepository mRepository;
    private final String hdPath = "M/44'/472'";
    protected AuthenticateModal.OnVerify.VerifyToken token;

    public MutableLiveData<BaseException> getObserverException() {
        return observerException;
    }

    private final MutableLiveData<BaseException> observerException = new MutableLiveData<>(null);

    public ArweaveTxViewModel(@NonNull Application application) {
        super(application);
        this.mApplication = application;
        this.mRepository = MainApplication.getApplication().getRepository();
    }

    private String requestId;

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    protected String getAuthToken() {
        String authToken = null;
        if (!TextUtils.isEmpty(token.password)) {
            authToken = new GetPasswordTokenCallable(token.password).call();
        } else if (token.signature != null) {
            String message = new GetMessageCallable().call();
            if (!TextUtils.isEmpty(message)) {
                try {
                    token.signature.update(Hex.decode(message));
                    byte[] signature = token.signature.sign();
                    byte[] rs = Util.decodeRSFromDER(signature);
                    if (rs != null) {
                        authToken = new VerifyFingerprintCallable(Hex.toHexString(rs)).call();
                    }
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
            }
        }
        AuthenticateModal.OnVerify.VerifyToken.invalid(token);
        return authToken;
    }


    private RustSigner initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new RustSigner(hdPath.toLowerCase(), authToken);
    }

    public MutableLiveData<Tx> parseTransaction(String jsonHex) {
        MutableLiveData<Tx> result = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String json = new String(Hex.decode(jsonHex), StandardCharsets.UTF_8);
                JSONObject object = new JSONObject(json);
                String owner = object.optString("owner");
                String pubkey = ArweaveViewModel.getARPublicKey();
                if (pubkey == null) {
                    observerException.postValue(new InvalidStateException(mApplication.getString(R.string.incorrect_tx_data), "invalid state, pubkey cannot find"));
                    result.postValue(new Tx());
                }
                String myOwner = ArweaveViewModel.formatHex(Hex.decode(pubkey));
                if (owner.equals("")) {
                    object.put("owner", myOwner);
                } else {
                    if (!owner.equalsIgnoreCase(myOwner)) {
                        observerException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, owner not match"));
                        result.postValue(new Tx());
                    }
                }
                String jsonString = ArweaveParser.parse(Hex.toHexString(object.toString().getBytes(StandardCharsets.UTF_8)));
                JSONObject rccObject = new JSONObject(jsonString);
                String status = rccObject.getString("status");
                if (status.equals("success")) {
                    result.postValue(Tx.fromRCC(rccObject));
                } else {
                    observerException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, transaction parsed failed"));
                    result.postValue(new Tx());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                observerException.postValue(new InvalidTransactionException(mApplication.getString(R.string.incorrect_tx_data), "invalid transaction, json error"));
                result.postValue(new Tx());
            }
        });
        return result;
    }

    public MutableLiveData<SignState> handleSign(Tx tx, int saltLen) {
        MutableLiveData<SignState> signState = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            signState.postValue(new SignState(STATE_SIGNING, null));
            RustSigner signer = initSigner();
            if (signer == null) {
                signState.postValue(new SignState(STATE_SIGN_FAIL, null));
                return;
            }
            String signature = signer.signRSA(tx.getSignatureData(), saltLen);
            try {
                String txId = ArweaveViewModel.formatHex(Util.sha256(Hex.decode(signature)));
                insertDB(signature, txId, tx);
                signState.postValue(new SignState(STATE_SIGN_SUCCESS, txId, signature));
            } catch (Exception e) {
                e.printStackTrace();
                signState.postValue(new SignState(STATE_SIGN_FAIL, null));
            } finally {
                new ClearTokenCallable().call();
            }
        });
        return signState;
    }

    public MutableLiveData<SignState> handleSignMessage(String message, int saltLen) {
        MutableLiveData<SignState> signState = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            signState.postValue(new SignState(STATE_SIGNING, null));
            RustSigner signer = initSigner();
            if (signer == null) {
                signState.postValue(new SignState(STATE_SIGN_FAIL, null));
                new ClearTokenCallable().call();
                return;
            }
            String signature = signer.signRSA(message, saltLen);
            signState.postValue(new SignState(STATE_SIGN_SUCCESS, null, signature));
            new ClearTokenCallable().call();
        });
        return signState;
    }

    private void insertDB(String signature, String txId, Tx tx) throws JSONException {
        TxEntity txEntity = generateTxEntity();
        txEntity.setTxId(txId);
        JSONObject rawTx = tx.getRawTx();
        rawTx.put("signature", ArweaveViewModel.formatHex(Hex.decode(signature)));
        rawTx.put("id", txId);
        txEntity.setSignedHex(rawTx.toString());
        txEntity.setAddition(
                new JSONObject().put("rawTx", tx.rawTx)
                        .put("parsedMessage", tx.parsedMessage)
                        .put("requestId", requestId)
                        .put("signature", signature)
                        .toString()
        );
        mRepository.insertTx(txEntity);
    }

    private TxEntity generateTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.AR.coinId());
        txEntity.setSignId(WatchWallet.getWatchWallet(mApplication).getSignId());
        txEntity.setCoinCode(Coins.AR.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(mRepository.getBelongTo());
        return txEntity;
    }

    protected long getUniversalSignIndex(Context context) {
        long current = Utilities.getPrefs(context).getLong("universal_sign_index", 0);
        Utilities.getPrefs(context).edit().putLong("universal_sign_index", current + 1).apply();
        return current;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static class Tx {
        private final String owner;
        private final String target;
        private final String quantity;
        private final String reward;
        private final String dataSize;
        private final String signatureData;
        private final boolean parseSuccess;
        private final JSONObject rawTx;
        private final JSONObject parsedMessage;

        public Tx(String owner, String target, String quantity, String reward, String dataSize, String signatureData, JSONObject rawTx, JSONObject parsedMessage) {
            this.owner = owner;
            this.target = target;
            this.quantity = quantity;
            this.reward = reward;
            this.dataSize = dataSize;
            this.signatureData = signatureData;
            this.rawTx = rawTx;
            this.parsedMessage = parsedMessage;
            this.parseSuccess = true;
        }

        public Tx() {
            this.parseSuccess = false;
            this.owner = null;
            this.target = null;
            this.quantity = null;
            this.reward = null;
            this.dataSize = null;
            this.signatureData = null;
            this.rawTx = null;
            this.parsedMessage = null;
        }

        public static Tx fromRCC(JSONObject object) throws JSONException {
            JSONObject json = object.getJSONObject("formatted_json");
            return new Tx(json.getString("owner"),
                    json.getString("target"),
                    json.getString("quantity"),
                    json.getString("reward"),
                    json.getString("data_size"),
                    json.getString("signature_data"), object.getJSONObject("raw_json"), json);
        }


        public String getOwner() {
            return owner;
        }

        public String getTarget() {
            return target;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getReward() {
            return reward;
        }

        public String getDataSize() {
            return dataSize;
        }

        public String getSignatureData() {
            return signatureData;
        }

        public boolean isParseSuccess() {
            return parseSuccess;
        }

        public JSONObject getRawTx() {
            return rawTx;
        }

        public JSONObject getParsedMessage() {
            return parsedMessage;
        }
    }
}
