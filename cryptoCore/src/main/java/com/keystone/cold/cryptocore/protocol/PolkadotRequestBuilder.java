package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.PolkadotProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

import java.util.List;

public class PolkadotRequestBuilder {
    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final PolkadotProtoc.Polkadot.Builder polkadot;
    private final PolkadotProtoc.ParsePolkadotTransaction.Builder parsePolkadotTransaction;
    private final PolkadotProtoc.InitialPolkadotDB.Builder initialPolkadotDB;
    private final PolkadotProtoc.GetPacketsTotal.Builder getPacketsTotal;
    private final PolkadotProtoc.DecodeSequence.Builder decodeSequence;
    private final PolkadotProtoc.HandleStub.Builder handleStub;
    private final PolkadotProtoc.ImportAddress.Builder importAddress;
    private final PolkadotProtoc.GetSignContent.Builder getSignContent;

    public PolkadotRequestBuilder () {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        polkadot = PolkadotProtoc.Polkadot.newBuilder();
        parsePolkadotTransaction = PolkadotProtoc.ParsePolkadotTransaction.newBuilder();
        initialPolkadotDB = PolkadotProtoc.InitialPolkadotDB.newBuilder();
        getPacketsTotal = PolkadotProtoc.GetPacketsTotal.newBuilder();
        decodeSequence = PolkadotProtoc.DecodeSequence.newBuilder();
        handleStub = PolkadotProtoc.HandleStub.newBuilder();
        importAddress = PolkadotProtoc.ImportAddress.newBuilder();
        getSignContent = PolkadotProtoc.GetSignContent.newBuilder();
    }
    public String build() {
        blockChainRequest.setPolkadot(polkadot);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public PolkadotRequestBuilder parseTransaction(String data, String dbPath) {
        parsePolkadotTransaction.setTransactionData(data);
        parsePolkadotTransaction.setDbPath(dbPath);
        polkadot.setParseTransaction(parsePolkadotTransaction);
        return this;
    }

    public PolkadotRequestBuilder initialDB(String dbPath) {
        initialPolkadotDB.setDbPath(dbPath);
        polkadot.setInitPolkadotDb(initialPolkadotDB);
        return this;
    }

    public PolkadotRequestBuilder getPacketsTotal(String scanned) {
        getPacketsTotal.setPayload(scanned);
        polkadot.setGetPacketsTotal(getPacketsTotal);
        return this;
    }

    public PolkadotRequestBuilder decodeSequence(List<String> payloads) {
        decodeSequence.addAllPayload(payloads);
        polkadot.setDecodeSequence(decodeSequence);
        return this;
    }

    public PolkadotRequestBuilder handleStub(String dbPath, int checksum) {
        handleStub.setDbPath(dbPath);
        handleStub.setChecksum(checksum);
        polkadot.setHandleStub(handleStub);
        return this;
    }

    public PolkadotRequestBuilder importAddress(String dbPath, String pubkey, String path) {
        importAddress.setDbPath(dbPath);
        importAddress.setPublicKey(pubkey);
        importAddress.setDerivationPath(path);
        polkadot.setImportAddress(importAddress);
        return this;
    }

    public PolkadotRequestBuilder getSignContent(String dbPath, int checksum) {
        getSignContent.setDbPath(dbPath);
        getSignContent.setChecksum(checksum);
        polkadot.setGetSignContent(getSignContent);
        return this;
    }
}
