package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.AptosProtoc;
import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CommonProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class AptosRequestBuilder {

    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final AptosProtoc.Aptos.Builder aptos;
    private final CommonProtoc.ParseTransaction.Builder parseTransaction;

    public AptosRequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        aptos = AptosProtoc.Aptos.newBuilder();
        parseTransaction = CommonProtoc.ParseTransaction.newBuilder();
    }
    public String build() {
        aptos.setParseTransaction(parseTransaction);
        blockChainRequest.setAptos(aptos);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public AptosRequestBuilder setData(String data) {
        parseTransaction.setData(data);
        return this;
    }
}
