package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.ui.views.AuthenticateModal;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;

public abstract class BaseTxViewModel<T> extends AndroidViewModel {

    public static final String STATE_SIGNING = "signing";
    public static final String STATE_SIGN_FAIL = "signing_fail";
    public static final String STATE_SIGN_SUCCESS = "signing_success";


    protected final DataRepository repository;

    protected final MutableLiveData<String> signState = new MutableLiveData<>();
    protected final MutableLiveData<String> rawFormatTx = new MutableLiveData<>();
    protected MutableLiveData<Boolean> isParsing = new MutableLiveData<>(null);

    protected AuthenticateModal.OnVerify.VerifyToken token;

    public MutableLiveData<String> getSignState() {
        return signState;
    }

    protected final MutableLiveData<BaseException> observableException = new MutableLiveData<>();
    protected final MutableLiveData<T> observableTransaction = new MutableLiveData<>();

    interface SignCallBack {
        void startSign();

        void onFail();

        void onSignTxSuccess();

        void onSignMsgSuccess();
    }

    protected final SignCallBack signCallBack = new SignCallBack() {
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
        public void onSignTxSuccess() {
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        }

        @Override
        public void onSignMsgSuccess() {
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        }
    };


    public BaseTxViewModel(@NonNull Application application) {
        super(application);
        repository = MainApplication.getApplication().getRepository();
    }

    public MutableLiveData<BaseException> getObservableException() {
        return observableException;
    }

    public MutableLiveData<T> getObservableTransaction() {
        return observableTransaction;
    }

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


    protected long getUniversalSignIndex(Context context) {
        long current = Utilities.getPrefs(context).getLong("universal_sign_index", 0);
        Utilities.getPrefs(context).edit().putLong("universal_sign_index", current + 1).apply();
        return current;
    }


    public abstract void parseTxData(Bundle bundle);

    public abstract MutableLiveData<JSONObject> parseMessage(Bundle bundle);

    public abstract void handleSign();

    public abstract void handleSignMessage();

    public abstract String getSignatureUR();

    public LiveData<String> getRawFormatTx() {
        return rawFormatTx;
    }

    public MutableLiveData<Boolean> getIsParsing() {
        return isParsing;
    }

}
