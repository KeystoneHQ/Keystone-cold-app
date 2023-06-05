package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.GetADAExtendedPublicKeyRequestProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class GetADAExtendedPublicKeyRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final GetADAExtendedPublicKeyRequestProtoc.GetADAExtendedPublicKeyRequest.Builder getADAExtendedPublicKeyRequest;

    public GetADAExtendedPublicKeyRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        getADAExtendedPublicKeyRequest =  GetADAExtendedPublicKeyRequestProtoc.GetADAExtendedPublicKeyRequest.newBuilder();
    }
    public String build() {
        commandRequest.setGetAdaExtendedPublicKeyRequest(getADAExtendedPublicKeyRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public GetADAExtendedPublicKeyRequestBuilder setSignId(int id) {
        commandRequest.setRequestId(id);
        return this;
    }

    public GetADAExtendedPublicKeyRequestBuilder setGetAdaExtendedPublicKeyRequest(int seedId, String password, String path, String portName) {
        getADAExtendedPublicKeyRequest.setSeedId(seedId);
        getADAExtendedPublicKeyRequest.setPassword(password);
        getADAExtendedPublicKeyRequest.setPath(path);
        getADAExtendedPublicKeyRequest.setPortName(portName);
        return this;
    }
}
