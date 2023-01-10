package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import com.keystone.cold.model.Tx;

import java.util.List;

public interface TxLoader {
    List<Tx> load();
}
