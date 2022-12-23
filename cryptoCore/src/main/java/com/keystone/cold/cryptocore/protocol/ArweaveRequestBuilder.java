package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.ArweaveProtoc;
import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CommonProtoc;
import com.keystone.cold.cryptocore.CosmosProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class ArweaveRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final ArweaveProtoc.Arweave.Builder arweave;
    private final CommonProtoc.ParseTransaction.Builder parseTransaction;

    public ArweaveRequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        arweave = ArweaveProtoc.Arweave.newBuilder();
        parseTransaction = CommonProtoc.ParseTransaction.newBuilder();
    }
    public String build() {
        arweave.setParseTransaction(parseTransaction);
        blockChainRequest.setArweave(arweave);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public ArweaveRequestBuilder setData(String data) {
        parseTransaction.setData(data);
        return this;
    }
}
