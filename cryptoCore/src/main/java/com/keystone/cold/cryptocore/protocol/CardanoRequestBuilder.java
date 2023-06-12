package com.keystone.cold.cryptocore.protocol;

import com.keystone.cold.cryptocore.BlockChainRequestProtoc;
import com.keystone.cold.cryptocore.CardanoProtoc;
import com.keystone.cold.cryptocore.RCCABIProtoc;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

import java.util.List;

public class CardanoRequestBuilder {

    private final RCCABIProtoc.CommandRequest.Builder commandRequest;
    private final BlockChainRequestProtoc.BlockChainRequest.Builder blockChainRequest;
    private final CardanoProtoc.Cardano.Builder cardano;
    private final CardanoProtoc.ParseCardanoTransaction.Builder parseTransaction;
    private final CardanoProtoc.GenerateAddress.Builder generateAddress;

    public CardanoRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        cardano = CardanoProtoc.Cardano.newBuilder();
        parseTransaction = CardanoProtoc.ParseCardanoTransaction.newBuilder();
        generateAddress = CardanoProtoc.GenerateAddress.newBuilder();
    }

    public String build() {
        blockChainRequest.setCardano(cardano);
        commandRequest.setBlockChainRequest(blockChainRequest);
        byte[] data = commandRequest.build().toByteArray();
        return ByteFormatter.bytes2hex(data);
    }

    public CardanoRequestBuilder setTransactionData(String data, String xpub, String master_fingerprint, List<CardanoProtoc.CardanoUtxo> utxos, List<CardanoProtoc.CardanoCertKey> cardanoCertKeys) {
        parseTransaction.setData(data);
        parseTransaction.setXpub(xpub);
        parseTransaction.setMasterFingerprint(master_fingerprint);
        for (int i = 0; i < utxos.size(); i++) {
            parseTransaction.setUtxos(i, utxos.get(i));
        }
        for (int i = 0; i < cardanoCertKeys.size(); i++) {
            parseTransaction.setCertKeys(i, cardanoCertKeys.get(i));
        }
        cardano.setParseTransaction(parseTransaction);
        return this;
    }

    public CardanoRequestBuilder setGenerateAddress(String xpub, int index, int t) {
        generateAddress.setIndex(index);
        generateAddress.setT(t);
        generateAddress.setXpub(xpub);
        cardano.setGenerateAddress(generateAddress);
        return this;
    }
}
