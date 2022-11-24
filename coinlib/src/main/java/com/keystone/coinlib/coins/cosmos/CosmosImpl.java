package com.keystone.coinlib.coins.cosmos;

import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;

import org.bitcoinj.core.Sha256Hash;
import org.bouncycastle.util.encoders.Hex;

public class CosmosImpl implements Coin {

    private String coinCode;

    public CosmosImpl() {

    }
    public CosmosImpl(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public String coinCode() {
        return coinCode;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {

    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        byte[] data = Hex.decode(message);
        byte[] hashBytes = Sha256Hash.hash(data);
        String signingData = Hex.toHexString(hashBytes);
        String signature = signer.sign(signingData);
        if (signature != null) {
            return signature.substring(0, 128);
        } else {
            return null;
        }
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
        byte[] data = Hex.decode(hex);
        byte[] hashBytes = Sha256Hash.hash(data);
        String signingData = Hex.toHexString(hashBytes);
        String signature = signer.sign(signingData);
        if (signature != null) {
            signature = signature.substring(0, 128);
            return new SignTxResult(signature, hex, signature);
        } else {
            return null;
        }
    }
}
