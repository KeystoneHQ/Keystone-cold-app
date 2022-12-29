package com.keystone.cold.viewmodel.tx.psbt;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.coins.BTC.BtcImpl;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.tx.SignState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSBTViewModel extends AndroidViewModel {
    private final String TAG = "PSBTViewModel";

    public static final String STATE_NONE = "";
    public static final String STATE_SIGNING = "signing";
    public static final String STATE_SIGN_FAIL = "signing_fail";
    public static final String STATE_SIGN_SUCCESS = "signing_success";

    protected AuthenticateModal.OnVerify.VerifyToken token;

    protected static final String BTCLegacyPath = "M/44'/0'/0'";
    protected static final String BTCNestedSegwitPath = "M/49'/0'/0'";
    protected static final String BTCNativeSegwitPath = "M/84'/0'/0'";

    protected static final List<String> BTCPaths = new ArrayList<>(Arrays.asList(BTCLegacyPath, BTCNestedSegwitPath, BTCNativeSegwitPath));

    private final Application mApplication;
    private final DataRepository mRepository;

    public PSBTViewModel(@NonNull Application application) {
        super(application);
        this.mApplication = application;
        this.mRepository = MainApplication.getApplication().getRepository();
    }

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    public PSBT parsePsbtBase64(String psbt, String myMasterFingerprint) throws InvalidTransactionException {
        PSBT psbt1 = new PSBT(psbt, myMasterFingerprint);
        try {
            JSONObject object = new BtcImpl().parsePsbt(psbt);
            JSONArray inputs = object.getJSONArray("inputs");
            JSONArray outputs = object.getJSONArray("outputs");
            psbt1.adoptInputs(inputs);
            psbt1.adoptOutputs(outputs);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidTransactionException("Transaction data error");
        }
        return psbt1;
    }

    public MutableLiveData<SignState> handleSignPSBT(PSBT psbt) {
        MutableLiveData<SignState> signState = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = new SignCallback() {
                @Override
                public void startSign() {
                    signState.postValue(new SignState(STATE_SIGNING, null));
                }

                @Override
                public void onFail() {
                    signState.postValue(new SignState(STATE_SIGN_FAIL, null));
                    new ClearTokenCallable().call();
                }

                @Override
                public void onSuccess(String txId, String psbtB64) {
                    try {
                        TxEntity tx = adaptPSBTtoTxEntity(psbt);
                        if (TextUtils.isEmpty(txId)) {
                            txId = "unknown_txid_" + Math.abs(tx.hashCode());
                        }
                        tx.setTxId(txId);
                        tx.setSignedHex(psbtB64);
                        tx.setAddition(generateAddition(psbt));
                        mRepository.insertTx(tx);
                        signState.postValue(new SignState(STATE_SIGN_SUCCESS, txId));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        signState.postValue(new SignState(STATE_SIGN_FAIL, null));
                    }
                    new ClearTokenCallable().call();
                }

                @Override
                public void postProgress(int progress) {

                }
            };

            callback.startSign();

            Signer[] signer = initSigners(psbt);
            Btc btc = new Btc(new BtcImpl());
            btc.signPsbt(psbt.getRawData(), callback, signer);
        });
        return signState;
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

    private Signer[] initSigners(PSBT psbt) {
        List<PSBT.Input> inputs = psbt.getMySigningInputs();
        Signer[] signer = new Signer[inputs.size()];

        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        for (int i = 0; i < inputs.size(); i++) {
            signer[i] = new ChipSigner(inputs.get(i).getCanonicalHDPath().toLowerCase(), authToken, inputs.get(i).getCanonicalPubkey());
        }
        return signer;
    }

    private String getCoinCodeFromPSBT(PSBT psbt) {
        String canonicalPath = psbt.getMySigningInputs().get(0).getCanonicalHDPath();
        if (canonicalPath.startsWith(BTCLegacyPath)) {
            return Coins.BTC_LEGACY.coinCode();
        } else if (canonicalPath.startsWith(BTCNestedSegwitPath)) {
            return Coins.BTC.coinCode();
        } else {
            return Coins.BTC_NATIVE_SEGWIT.coinCode();
        }
    }

    private TxEntity adaptPSBTtoTxEntity(PSBT psbt) {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        String coinCode = getCoinCodeFromPSBT(psbt);
        tx.setSignId(WatchWallet.getWatchWallet(mApplication).getSignId());
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private String generateAddition(PSBT psbt) throws JSONException {
        JSONObject parsedMessage = psbt.generateParsedMessage();
        JSONObject addition = new JSONObject();
        addition.put("raw_message", psbt.getRawData());
        addition.put("parsed_messsage", parsedMessage);
        return addition.toString();
    }

}


