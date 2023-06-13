package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.GetRsaPublicKeyRequestProtoc;
import com.keystone.cold.cryptocore.SignRequestProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.cryptocore.RCCABIProtoc;

public class SignRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final SignRequestProtoc.SignRequest.Builder signRequest;

    public SignRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        signRequest = SignRequestProtoc.SignRequest.newBuilder();
    }
    public String build() {
        commandRequest.setSignRequest(signRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public SignRequestBuilder setSignId(int id) {
        commandRequest.setRequestId(id);
        return this;
    }

    public SignRequestBuilder setSignRequest(int seedId, int algoValue, String password, String path, String data, String portName) {
        signRequest.setSeedId(seedId);
        signRequest.setAlgoValue(algoValue);
        signRequest.setPassword(password);
        signRequest.setDerivationPath(path);
        signRequest.setData(data);
        signRequest.setPortName(portName);
        signRequest.putSigningOption("salt_len", "0");
        return this;
    }

    public SignRequestBuilder setADASignRequest(int seedId, String password, String path, String data, String portName) {
        signRequest.setSeedId(seedId);
        signRequest.setAlgoValue(2);
        signRequest.setPassword(password);
        signRequest.setDerivationPath(path);
        signRequest.setData(data);
        signRequest.setPortName(portName);
        signRequest.putSigningOption("sign_ada", "true");
        return this;
    }
}
