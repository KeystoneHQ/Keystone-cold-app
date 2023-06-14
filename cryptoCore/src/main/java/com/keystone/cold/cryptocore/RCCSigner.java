package com.keystone.cold.cryptocore;

import android.text.TextUtils;
import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.SignRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;

import java.util.Objects;

// Rust Signer currently only support K1 curve
public class RCCSigner {

    static {
        System.loadLibrary("rcc_android");
    }

    private final String privKeyPath;
    private final String authToken;
    private final boolean isMainWallet;
    private final String portName;
    private final int requestId;

    public RCCSigner(String path, String authToken, boolean isMainWallet, String portName, int requestId) {
        this.privKeyPath = Objects.requireNonNull(path);
        this.authToken = authToken;
        this.isMainWallet = isMainWallet;
        this.portName = portName;
        this.requestId = requestId;
    }

    public String sign(String data) {
        return sign(data, SignAlgo.SECP256K1, 0);
    }

    public String sign(String data, SignAlgo algo, int saltLen) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        RCC rcc = new RCC();
        String command = composeCommand(data, algo);

        Log.e("Rust Signer:", command);

        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public String signADA(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        RCC rcc = new RCC();
        SignRequestBuilder rb = new SignRequestBuilder();
        rb.setSignId(requestId);
        int seedId = isMainWallet ? 0 : 0x50;
        rb.setADASignRequest(seedId, authToken, privKeyPath, data, portName);
        String command =  rb.build();
        Log.e("Rust Signer:", command);
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    private String composeCommand(String data, SignAlgo algo) {
        SignRequestBuilder rb = new SignRequestBuilder();
        rb.setSignId(requestId);
        int seedId = isMainWallet ? 0 : 0x50;
        rb.setSignRequest(seedId, algo.getValue(), authToken, privKeyPath, data, portName);
        return rb.build();
    }

    private String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int responseId = parser.getResponseId();
        int status = parser.getStatus();
        if (responseId == requestId && status == 200) {
            response = parser.getResponse();
            Log.e("rcc Signer reponse:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("rcc Signer Error:", error);
            return null;
        }
    }

    public enum SignAlgo {
        SECP256K1("secp256k1", 0),
        ED25519("ed25519", 2),
        RSA("rsa", 3);

        private String name;
        private int value;

        SignAlgo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
