package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.cryptocore.SetupADARootKeyRequestProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class SetupADARootKeyRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final SetupADARootKeyRequestProtoc.SetupADARootKeyRequest.Builder setupADARootKeyRequest;

    public SetupADARootKeyRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        setupADARootKeyRequest =  SetupADARootKeyRequestProtoc.SetupADARootKeyRequest.newBuilder();
    }
    public String build() {
        commandRequest.setSetupAdaRootKeyRequest(setupADARootKeyRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public SetupADARootKeyRequestBuilder setSignId(int id) {
        commandRequest.setRequestId(id);
        return this;
    }

    public SetupADARootKeyRequestBuilder setSetupADARootKeyRequest(int seedId, String password, String passphrase, String portName) {
        setupADARootKeyRequest.setSeedId(seedId);
        setupADARootKeyRequest.setPassword(password);
        setupADARootKeyRequest.setPassphrase(passphrase);
        setupADARootKeyRequest.setPortName(portName);
        return this;
    }
}
