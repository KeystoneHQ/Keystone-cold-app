package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.GetRsaPublicKeyRequestProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class GetRSAPublicKeyRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final GetRsaPublicKeyRequestProtoc.GetRsaPublicKeyRequest.Builder getRSAPublicKeyRequest;

    public GetRSAPublicKeyRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        getRSAPublicKeyRequest = GetRsaPublicKeyRequestProtoc.GetRsaPublicKeyRequest.newBuilder();
    }
    public String build() {
        commandRequest.setGetRsaPublicKeyRequest(getRSAPublicKeyRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public GetRSAPublicKeyRequestBuilder setSignId(int id) {
        commandRequest.setRequestId(id);
        return this;
    }

    public GetRSAPublicKeyRequestBuilder setGetRSAPublicKeyRequest(int seedId, String password, String portName) {
        getRSAPublicKeyRequest.setSeedId(seedId);
        getRSAPublicKeyRequest.setPassword(password);
        getRSAPublicKeyRequest.setPortName(portName);
        return this;
    }
}
