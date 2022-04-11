package com.keystone.coinlib.coins.SOL;

import static com.keystone.coinlib.Util.concat;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionDecoder;

public class SolImpl implements Coin {
    @Override
    public String coinCode() {
        return Coins.SOL.coinCode();
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
