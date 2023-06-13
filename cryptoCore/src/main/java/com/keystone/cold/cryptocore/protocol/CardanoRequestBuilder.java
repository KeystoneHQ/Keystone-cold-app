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
    private final CardanoProtoc.DerivePublicKey.Builder derivePublicKey;
    private final CardanoProtoc.ComposeWitnessSet.Builder composeWitnessSet;

    public CardanoRequestBuilder() {
        commandRequest = RCCABIProtoc.CommandRequest.newBuilder();
        blockChainRequest = BlockChainRequestProtoc.BlockChainRequest.newBuilder();
        cardano = CardanoProtoc.Cardano.newBuilder();
        parseTransaction = CardanoProtoc.ParseCardanoTransaction.newBuilder();
        generateAddress = CardanoProtoc.GenerateAddress.newBuilder();
        derivePublicKey = CardanoProtoc.DerivePublicKey.newBuilder();
        composeWitnessSet = CardanoProtoc.ComposeWitnessSet.newBuilder();
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
        parseTransaction.addAllCertKeys(cardanoCertKeys);
        parseTransaction.addAllUtxos(utxos);
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

    public CardanoRequestBuilder setDerivePublicKey(String xpub, String subPath) {
        derivePublicKey.setXpub(xpub);
        derivePublicKey.setSubPath(subPath);
        cardano.setDerivePublicKey(derivePublicKey);
        return this;
    }

    public CardanoRequestBuilder setComposeWitnessSet(List<CardanoProtoc.CardanoSignature> signatures) {
        composeWitnessSet.addAllSignatures(signatures);
        cardano.setComposeWitnessSet(composeWitnessSet);
        return this;
    }
}
