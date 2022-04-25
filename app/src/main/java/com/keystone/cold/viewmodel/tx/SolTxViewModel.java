package com.keystone.cold.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.encryption.ChipSigner;

import org.json.JSONException;
import org.json.JSONObject;

public class SolTxViewModel extends Base {
    private String txHex;
    private String messageData;
    private String hdPath;
    private int chainId;

    private String requestId;
    private String signature;

    public SolTxViewModel(@NonNull Application application) {
        super(application);
        coinCode = "SOL";
    }

    private Web3TxViewModel.SignCallBack signCallBack = new Web3TxViewModel.SignCallBack() {
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
        public void onSignTxSuccess(String txId, String signedTxHex, String signatureHex) {
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
        SignTxResult result = new SolImpl().signHex(txHex, signer);
        Log.d("sora", "signTransaction: " + result);
        if (result == null) {
            signCallBack.onFail();
        } else {
            Log.d("sora", "signTransaction: " + result.signaturHex);
            signCallBack.onSignTxSuccess(result.txId, result.txHex, result.signaturHex);
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

    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(SIGN_DATA);
            hdPath = bundle.getString(HD_PATH);
            requestId = bundle.getString(REQUEST_ID);

            SolImpl.parseMessage(txHex);
        });
    }
}
