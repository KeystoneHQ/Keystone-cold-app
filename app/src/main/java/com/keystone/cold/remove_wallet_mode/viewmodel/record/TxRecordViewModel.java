/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.remove_wallet_mode.viewmodel.record;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.helper.CoinConfigHelper;
import com.keystone.cold.remove_wallet_mode.helper.TxRecordManager;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class TxRecordViewModel extends AndroidViewModel {

    public TxRecordViewModel(@NonNull Application application) {
        super(application);

    }

    public LiveData<List<Tx>> loadTxs(String coinId) {
        List<AssetItem> evmChains = CoinConfigHelper.getEVMChains();
        Optional<AssetItem> evmChain = evmChains.stream().filter(v -> v.getCoinId().equals(coinId)).findFirst();
        if (evmChain.isPresent() || coinId.equals(Coins.ETH.coinId())) {
            return loadEVMTxs(coinId);
        }
        return loadCommonTxs(coinId);
    }

    private LiveData<List<Tx>> loadCommonTxs(String coinId) {
        MutableLiveData<List<Tx>> txs = new MutableLiveData<>();
        TxRecordManager.loadTx(coinId, txs);
        return txs;
    }

    private LiveData<List<Tx>> loadEVMTxs(String coinId) {
        MutableLiveData<List<Tx>> txs = new MutableLiveData<>();
        TxRecordManager.loadTx(Coins.ETH.coinId(), txs);
        return Transformations.map(txs, (v) -> v.stream().filter(tx -> {
            GenericETHTxEntity ethTxEntity = (GenericETHTxEntity) tx;
            String signData = ethTxEntity.getSignedHex();
            EthereumTransaction transaction = null;
            switch (ethTxEntity.getTxType()) {
                case 0x00: {
                    transaction = EthereumTransaction.generateLegacyTransaction(signData, null, true);
                    break;
                }
                case 0x02: {
                    transaction = EthereumTransaction.generateFeeMarketTransaction(signData, null);
                    break;
                }
                default:
                    break;
            }
            if (transaction != null) {
                AssetItem chain = transaction.getAssetItem();
                if (chain == null) {
                    //this transaction belongs to ETH if we do not know this chain.
                    return coinId.equals(Coins.ETH.coinId());
                }
                //only show transaction belongs to this chain
                return coinId.equals(chain.getCoinId());
            } else {
                //should not goes here;
                return false;
            }
        }).collect(Collectors.toList()));
    }
}
