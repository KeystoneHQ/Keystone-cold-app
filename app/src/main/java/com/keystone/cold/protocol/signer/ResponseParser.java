package com.keystone.cold.protocol.signer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;
import com.keystone.cold.protobuf.SignerABIProtoc;

public class ResponseParser {
    private final byte[] protoBytes;
    private final SignerABIProtoc.CommandResponse cp;

    public ResponseParser(String protoString) {
        SignerABIProtoc.CommandResponse result;
        protoBytes = ByteFormatter.hex2bytes(protoString);
        try {
            result= SignerABIProtoc.CommandResponse.parseFrom(protoBytes);

        } catch  (InvalidProtocolBufferException e) {
            e.printStackTrace();
            result =  null;
        }
        cp = result;
    }

    public int getResponseId() {

        return cp.getResponseId();
    }

    public int getStatus() {
        Preconditions.checkNotNull(cp);
        return cp.getStatus();
    }

    public String getResponse() {
        Preconditions.checkNotNull(cp);
        return cp.getResponse();
    }

    public String getError() {
        Preconditions.checkNotNull(cp);
        return cp.getErrorMessage();
    }

}
