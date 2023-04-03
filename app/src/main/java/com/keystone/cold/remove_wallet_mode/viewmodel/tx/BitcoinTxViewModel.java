package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.coins.BTC.BtcImpl;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin.PSBT;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitcoinTxViewModel extends BaseTxViewModel<PSBT> {
    private static final String TAG = "BitcoinTxViewModel";

    public static final String BTCLegacyPath = "M/44'/0'/0'";
    public static final String BTCNestedSegwitPath = "M/49'/0'/0'";
    public static final String BTCNativeSegwitPath = "M/84'/0'/0'";
    public static final String BTCCoreNativeSegwitPath = "M/44'/60'/0'";

    public static final List<String> BTCPaths = new ArrayList<>(Arrays.asList(BTCLegacyPath, BTCNestedSegwitPath, BTCNativeSegwitPath));

    private static final String RAW_PSBT = "raw_message";

    private final DataRepository mRepository;
    private String signedPSBT;

    public String getCoinCode() {
        return coinCode;
    }

    private String coinCode;

    public BitcoinTxViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = MainApplication.getApplication().getRepository();
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                isParsing.postValue(true);
                String psbtB64 = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                String mfp = new GetMasterFingerprintCallable().call();
                PSBT psbt = parsePsbtBase64(psbtB64, mfp);
                rawFormatTx.postValue(psbtB64);
                coinCode = getCoinCodeFromPSBT(psbt);
                observableTransaction.postValue(psbt);
            } catch (BaseException e) {
                observableException.postValue(e);
            } finally {
                isParsing.postValue(false);
            }
        });
    }

    public void parseExistingTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                isParsing.postValue(true);
                TxEntity tx = mRepository.loadTxSync(txId);
                signedPSBT = tx.getSignedHex();
                rawFormatTx.postValue(signedPSBT);
                JSONObject object = new JSONObject(tx.getAddition());
                String psbtB64 = object.getString("raw_message");
                String mfp = new GetMasterFingerprintCallable().call();
                PSBT psbt = parsePsbtBase64(psbtB64, mfp);
                psbt.setSignedBase64(signedPSBT);
                coinCode = tx.getCoinCode();
                observableTransaction.postValue(psbt);
            } catch (BaseException e) {
                observableException.postValue(e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                isParsing.postValue(false);
            }
        });

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
            throw new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "Transaction data error");
        }
        return psbt1;
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            PSBT psbt = observableTransaction.getValue();
            if (psbt == null) return;
            SignCallback callback = new SignCallback() {
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
                public void onSuccess(String txId, String psbtB64) {
                    try {
                        TxEntity tx = adaptPSBTtoTxEntity(psbt);
                        if (TextUtils.isEmpty(txId)) {
                            txId = "unknown_txid_" + Math.abs(tx.hashCode());
                        }
                        tx.setTxId(txId);
                        tx.setSignedHex(psbtB64);
                        signedPSBT = psbtB64;
                        tx.setAddition(generateAddition(psbt));
                        mRepository.insertTx(tx);
                        signState.postValue(STATE_SIGN_SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        signState.postValue(STATE_SIGN_FAIL);
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

    private TxEntity adaptPSBTtoTxEntity(PSBT psbt) {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        // TODO get origin and update this line.
        tx.setSignId(Wallet.UNKNOWN_WALLET_SIGN_ID);
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private String generateAddition(PSBT psbt) throws JSONException {
        JSONObject parsedMessage = psbt.generateParsedMessage();
        JSONObject addition = new JSONObject();
        addition.put(RAW_PSBT, psbt.getRawData());
        addition.put("parsed_messsage", parsedMessage);
        return addition.toString();
    }

    public static String getCoinCodeFromPSBT(PSBT psbt) {
        // TODO add LTC support;
        String canonicalPath = psbt.getMySigningInputs().get(0).getCanonicalHDPath().toUpperCase();
        if (canonicalPath.startsWith(BTCLegacyPath)) {
            return Coins.BTC_LEGACY.coinCode();
        } else if (canonicalPath.startsWith(BTCNestedSegwitPath)) {
            return Coins.BTC.coinCode();
        } else if (canonicalPath.startsWith(BTCCoreNativeSegwitPath)) {
            return Coins.BTC_CORE_WALLET.coinCode();
        } else {
            return Coins.BTC_NATIVE_SEGWIT.coinCode();
        }
    }

    public void reset() {
        observableTransaction.postValue(null);
        observableException.postValue(null);
    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        CryptoPSBT cryptoPSBT = new CryptoPSBT(Base64.decode(signedPSBT));
        return cryptoPSBT.toUR().toString();
    }
}
