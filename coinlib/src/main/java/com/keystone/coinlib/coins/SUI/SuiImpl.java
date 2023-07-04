package com.keystone.coinlib.coins.SUI;

import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;

import org.spongycastle.util.encoders.Hex;

public class SuiImpl implements Coin {

    @Override
    public String coinCode() {
        return null;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {}

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        byte[] hash = Util.blake2b256(Hex.decode(message));
        String hashHex = Hex.toHexString(hash);
        return signer.sign(hashHex);
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
