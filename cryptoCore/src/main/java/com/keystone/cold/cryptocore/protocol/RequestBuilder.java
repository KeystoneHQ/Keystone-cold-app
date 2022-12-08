package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.GetRSAPublicKeyProtoc;
import com.keystone.cold.cryptocore.SignRequestProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.cryptocore.RCCABIProtoc;



public class RequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final SignRequestProtoc.SignRequest.Builder signRequest;
    private final GetRSAPublicKeyProtoc.GetRSAPublicKey.Builder getRSAPublicKey;

    public RequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        signRequest = SignRequestProtoc.SignRequest.newBuilder();
        getRSAPublicKey = GetRSAPublicKeyProtoc.GetRSAPublicKey.newBuilder();
    }
    public String build() {
        commandRequest.setSignRequest(signRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public RequestBuilder setSignId(int id) {
        commandRequest.setRequestId(id);
        return this;
    }

    public RequestBuilder setSignRequest(int seedId, int algoValue, String password, String path, String data, String portName) {
        signRequest.setSeedId(seedId);
        signRequest.setAlgoValue(algoValue);
        signRequest.setPassword(password);
        signRequest.setDerivationPath(path);
        signRequest.setData(data);
        signRequest.setPortName(portName);
        return this;
    }

    public RequestBuilder setGetRSAPublicKey(int seedId, String password, String portName) {
        getRSAPublicKey.setSeedId(seedId);
        getRSAPublicKey.setPassword(password);
        getRSAPublicKey.setPortName(portName);
        return this;
    }
}
