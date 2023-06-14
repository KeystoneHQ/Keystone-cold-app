package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;


import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.cardano.CardanoCertKey;

import java.io.Serializable;

public class CardanoCertificate extends CardanoCertKey implements Serializable {
    public CardanoCertificate(byte[] keyHash, CryptoKeypath keypath) {
        super(keyHash, keypath);
    }

    public static CardanoCertificate fromUR(CardanoCertKey certKey) {
        return new CardanoCertificate(certKey.getKeyHash(), certKey.getKeypath());
    }
}
