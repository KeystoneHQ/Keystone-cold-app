package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CommonProtoc;
import com.keystone.cold.cryptocore.NearProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class NearRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final NearProtoc.Near.Builder near;
    private final CommonProtoc.ParseTransaction.Builder parseTransaction;

    public NearRequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        near = NearProtoc.Near.newBuilder();
        parseTransaction = CommonProtoc.ParseTransaction.newBuilder();
    }
    public String build() {
        near.setParseTransaction(parseTransaction);
        blockChainRequest.setNear(near);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public NearRequestBuilder setData(String data) {
        parseTransaction.setData(data);
        return this;
    }


}
