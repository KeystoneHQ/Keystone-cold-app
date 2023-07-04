package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.AptosProtoc;
import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CommonProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.cryptocore.SuiProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class SuiRequestBuilder {

    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final SuiProtoc.Sui.Builder sui;
    private final CommonProtoc.ParseTransaction.Builder parseTransaction;

    public enum Type {
        Transaction,
        Message
    }

    public SuiRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        sui = SuiProtoc.Sui.newBuilder();
        parseTransaction = CommonProtoc.ParseTransaction.newBuilder();
    }
    public String build(Type type) {
        switch (type) {
            case Message:
                sui.setParseMessage(parseTransaction);
                break;
            case Transaction:
            default:
                sui.setParseTransaction(parseTransaction);
                break;
        }
        blockChainRequest.setSui(sui);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public SuiRequestBuilder setData(String data) {
        parseTransaction.setData(data);
        return this;
    }
}
