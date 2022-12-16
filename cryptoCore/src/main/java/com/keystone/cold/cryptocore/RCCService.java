package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.RequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;

public class RCCService {
    private static final String TAG = "RCCService";

    static {
        System.loadLibrary("rcc_android");
    }

    public static class Passport {
        private final String authToken;
        private final boolean isMainWallet;
        private final String portName;
        private final int FixedRequestId = 305;

        public Passport(String authToken, boolean isMainWallet, String portName) {
            this.authToken = authToken;
            this.isMainWallet = isMainWallet;
            this.portName = portName;
        }
    }

    public static RCCSigner createSigner(String path, Passport passport) {
        return new RCCSigner(path, passport.authToken, passport.isMainWallet, passport.portName, passport.FixedRequestId);
    }

    public static String getRSAPublicKey(Passport passport) {
        RequestBuilder rb = new RequestBuilder();
        rb.setSignId(passport.FixedRequestId);
        int seedId = passport.isMainWallet? 0 : 0x50;
        Log.d(TAG, "getRSAPublicKey: " + seedId);
        Log.d(TAG, "getRSAPublicKey: " + passport.authToken);
        Log.d(TAG, "getRSAPublicKey: " + passport.portName);
        rb.setGetRSAPublicKeyRequest(seedId, passport.authToken, passport.portName);
        String command = rb.build();

        Log.e("Rust Signer:", command);

        RCC rcc = new RCC();
        String response = rcc.processCommand(command);
        return parseResponse(response, passport.FixedRequestId);
    }

    private static String parseResponse(String response, int requestId) {
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
}
