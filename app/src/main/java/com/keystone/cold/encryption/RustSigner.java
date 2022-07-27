package com.keystone.cold.encryption;

import android.util.Log;

import androidx.annotation.Nullable;

import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.cryptocore.RCCSigner;

// Rust Signer currently only support K1 curve
public class RustSigner extends Signer {

    private final RCCSigner rccSigner;

    public RustSigner(String path, String authToken) {
        this(path, authToken, null);
    }

    public RustSigner(String path, String authToken, @Nullable String publicKey) {
        super(publicKey);
        boolean isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
        String portName =  EncryptionCoreProvider.getInstance().getPortName();
        rccSigner = new RCCSigner(path, authToken, isMainWallet, portName);
    }


    @Override
    public String sign(String data) {
        return rccSigner.sign(data);
    }

}
