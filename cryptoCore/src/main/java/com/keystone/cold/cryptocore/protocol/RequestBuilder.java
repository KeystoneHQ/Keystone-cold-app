package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.cryptocore.RCCABIProtoc;



public class RequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final RCCABIProtoc.SignRequest.Builder signRequest;

    public RequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        signRequest = RCCABIProtoc.SignRequest.newBuilder();
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

}
