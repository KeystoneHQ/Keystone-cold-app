package com.keystone.cold.protocol.signer;

import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.protobuf.SignerABIProtoc;


public class RequestBuilder {
    private final SignerABIProtoc.CommandRequest.Builder commandRequest;
    private final SignerABIProtoc.SignRequest.Builder signRequest;

    public RequestBuilder () {
        commandRequest = SignerABIProtoc.CommandRequest.newBuilder();
        signRequest = SignerABIProtoc.SignRequest.newBuilder();
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
