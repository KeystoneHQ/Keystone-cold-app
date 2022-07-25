package com.keystone.coinlib.coins.NEAR;

import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;

public class NearImpl implements Coin {

    static {
        System.loadLibrary("coin_android");
    }
    public interface ParseMessageCallback {

        void onSuccess(String json);

        void onFailed(String error);

    }
    protected static native int nativeParseMessage(final String message, final ParseMessageCallback callback);

    public static void parseMessage(String message, ParseMessageCallback parseMessageCallback) {
        int status = nativeParseMessage(message, parseMessageCallback);
        Log.w("Near", "parseMessage: status=" + status);
    }

    @Override
    public String coinCode() {
        return null;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {

    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return null;
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return null;
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return false;
    }


    public SignTxResult signHex(String hex, Signer signer) {
        String signature = signer.sign(hex);
        if (signature != null) {
            return new SignTxResult(signature, hex, signature);
        } else {
            return null;
        }
    }
}
