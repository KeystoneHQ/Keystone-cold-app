package com.keystone.cold.cryptocore;

import android.text.TextUtils;
import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.RequestBuilder;
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
    private final int FixedRequestId = 305;

    public RCCSigner(String path, String authToken, boolean isMainWallet, String portName) {
        this.privKeyPath = Objects.requireNonNull(path);
        this.authToken = authToken;
        this.isMainWallet = isMainWallet;
        this.portName =  portName;
    }


    public String sign(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        RCC rcc = new RCC();
        String command = composeCommand(data);

        Log.e("Rust Signer:", command);

        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    private String composeCommand(String data) {
        RequestBuilder rb = new RequestBuilder();
        rb.setSignId(FixedRequestId);
        int seedId = isMainWallet? 0 : 0x50;
        rb.setSignRequest(seedId, 0, authToken, privKeyPath, data, portName);
        return rb.build();
    }

    private String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int responseId = parser.getResponseId();
        int status = parser.getStatus();
        if (responseId == FixedRequestId && status == 200) {
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
