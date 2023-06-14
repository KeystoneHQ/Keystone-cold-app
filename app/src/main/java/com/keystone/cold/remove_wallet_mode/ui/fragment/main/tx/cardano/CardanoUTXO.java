package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.cardano.CardanoUtxo;

import java.io.Serializable;

public class CardanoUTXO extends CardanoUtxo implements Serializable {
    public CardanoUTXO(byte[] transactionHash, int index, long value, CryptoKeypath path, String address) {
        super(transactionHash, index, value, path, address);
    }

    public static CardanoUTXO fromUR(CardanoUtxo cardanoUtxo) {
        return new CardanoUTXO(cardanoUtxo.getTransactionHash(), cardanoUtxo.getIndex(), cardanoUtxo.getValue(), cardanoUtxo.getPath(), cardanoUtxo.getAddress());
    }
}
