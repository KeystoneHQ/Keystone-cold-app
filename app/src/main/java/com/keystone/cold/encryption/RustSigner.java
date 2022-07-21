package com.keystone.cold.encryption;

import android.util.Log;

import androidx.annotation.Nullable;

import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;
import com.keystone.cold.encryptioncore.job.JobSchedulerImpl;
import com.keystone.cold.lib.KeystoneSigner;
import com.keystone.cold.protocol.signer.RequestBuilder;
import com.keystone.cold.protocol.signer.ResponseParser;
import java.util.Objects;

// Rust Signer currently only support K1 curve
public class RustSigner extends Signer {

    static {
        System.loadLibrary("keystone_signer");
    }

    private final String privKeyPath;
    private final String authToken;
    private final boolean isMainWallet;
    private final String portName;

    public RustSigner(String path, String authToken) {
        this(path, authToken, null);
    }

    public RustSigner(String path, String authToken, @Nullable String publicKey) {
        super(publicKey);
        this.privKeyPath = Objects.requireNonNull(path);
        this.authToken = authToken;
        this.isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
        this.portName =  EncryptionCoreProvider.getInstance().getPortName();
    }


    @Override
    public String sign(String data) {
        KeystoneSigner ks = new KeystoneSigner();
        String command = composeCommand(data);

        Log.e("Rust Signer:", command);

        String response = ks.processCommand(command);
        return parseResponse(response);
    }

    private String composeCommand(String data) {
        RequestBuilder rb = new RequestBuilder();
        rb.setSignId(305);
        int seedId = isMainWallet? 0 : 0x50;
        rb.setSignRequest(seedId, 0, authToken, privKeyPath, data, portName);
        return rb.build();
    }

    private String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int responseId = parser.getResponseId();
        int status = parser.getStatus();
        if (responseId == 305 && status == 200) {
            response = parser.getResponse();
            Log.e("Rust Signer reponse:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("Rust Signer Error:", error);
            return null;
        }
    }

}
