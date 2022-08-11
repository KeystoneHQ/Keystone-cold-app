package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CommonProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.cryptocore.SolanaProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

public class SolanaRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final SolanaProtoc.Solana.Builder solana;
    private final CommonProtoc.ParseTransaction.Builder parseTransaction;

    public SolanaRequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        solana = SolanaProtoc.Solana.newBuilder();
        parseTransaction = CommonProtoc.ParseTransaction.newBuilder();
    }
    public String build() {
        solana.setParseTransaction(parseTransaction);
        blockChainRequest.setSolana(solana);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public SolanaRequestBuilder setData(String data) {
        parseTransaction.setData(data);
        return this;
    }

}
