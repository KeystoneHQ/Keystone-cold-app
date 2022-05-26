package com.keystone.coinlib;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;

import org.json.JSONObject;

public class CoinImpl implements Coin{
    protected static native Void nativeLegacyGenerateTransaction(final String tx, SignCallback sc, Signer signer, String CoinCode);

    static {
        System.loadLibrary("CryptoCoinLib_v0_1_3");
    }

    @Override
    public String coinCode() {
        return null;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject txObj = tx.getMetaData();
        try {
            txObj.put("signingPubKey", signers[0].getPublicKey());
            String txStr = txObj.toString();
            nativeLegacyGenerateTransaction(txStr,callback,signers[0],this.coinCode());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
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
}