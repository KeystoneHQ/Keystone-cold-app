package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.GetADAExtendedPublicKeyRequestBuilder;
import com.keystone.cold.cryptocore.protocol.GetRSAPublicKeyRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;
import com.keystone.cold.cryptocore.protocol.SetupADARootKeyRequestBuilder;

public class RCCService {
    private static final String TAG = "RCCService";

    static {
        System.loadLibrary("rcc_android");
    }

    public static class Passport {
        private final String tokenOrPassword;
        private final boolean isMainWallet;
        private final String portName;
        private final int FixedRequestId = 305;

        public Passport(String tokenOrPassword, boolean isMainWallet, String portName) {
            this.tokenOrPassword = tokenOrPassword;
            this.isMainWallet = isMainWallet;
            this.portName = portName;
        }
    }

    public static RCCSigner createSigner(String path, Passport passport) {
        return new RCCSigner(path, passport.tokenOrPassword, passport.isMainWallet, passport.portName, passport.FixedRequestId);
    }

    public static String getRSAPublicKey(Passport passport) {
        GetRSAPublicKeyRequestBuilder rb = new GetRSAPublicKeyRequestBuilder();
        rb.setSignId(passport.FixedRequestId);
        int seedId = passport.isMainWallet ? 0 : 0x50;
        rb.setGetRSAPublicKeyRequest(seedId, passport.tokenOrPassword, passport.portName);
        String command = rb.build();

        Log.i("RCCService.getRSAPublicKey: ", command);

        RCC rcc = new RCC();
        String response = rcc.processCommand(command);
        return parseResponse(response, passport.FixedRequestId);
    }

    public static String setupADARootKey(String passphrase, Passport passport) {
        SetupADARootKeyRequestBuilder rb = new SetupADARootKeyRequestBuilder();
        rb.setSignId(passport.FixedRequestId);
        int seedId = passport.isMainWallet ? 0 : 0x50;
        rb.setSetupADARootKeyRequest(seedId, passport.tokenOrPassword, passphrase, passport.portName);
        String command = rb.build();

        Log.i("RCCService.setupADARootKey: ", command);

        RCC rcc = new RCC();
        String response = rcc.processCommand(command);
        return parseResponse(response, passport.FixedRequestId);
    }

    public static String getADAExtendedPublicKey(String path, Passport passport) {
        GetADAExtendedPublicKeyRequestBuilder rb = new GetADAExtendedPublicKeyRequestBuilder();
        rb.setSignId(passport.FixedRequestId);
        int seedId = passport.isMainWallet ? 0 : 0x50;
        rb.setGetAdaExtendedPublicKeyRequest(seedId, passport.tokenOrPassword, path, passport.portName);
        String command = rb.build();

        Log.i("RCCService.getADAExtendedPublicKey: ", command);

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
            Log.i("rcc Signer reponse:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("rcc Signer Error:", error);
            return null;
        }
    }
}
