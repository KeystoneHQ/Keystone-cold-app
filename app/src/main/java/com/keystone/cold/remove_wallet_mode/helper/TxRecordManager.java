package com.keystone.cold.remove_wallet_mode.helper;

import static com.keystone.cold.MainApplication.getApplication;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.helper.tx_loader.EthTxLoader;
import com.keystone.cold.remove_wallet_mode.helper.tx_loader.GeneralTxLoader;
import com.keystone.cold.remove_wallet_mode.helper.tx_loader.NearTxLoader;
import com.keystone.cold.remove_wallet_mode.helper.tx_loader.SolTxLoader;
import com.keystone.cold.remove_wallet_mode.helper.tx_loader.TxLoader;

import java.util.List;

public class TxRecordManager {

    public static void loadTx(String coinId, MutableLiveData<List<Tx>> txRecords) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxLoader txLoader = getTxLoader(coinId);
            txRecords.postValue(txLoader.load());
        });
    }

    private static TxLoader getTxLoader(String coinId) {
        if (Coins.ETH.coinId().equals(coinId)) {
            String code = Utilities.getCurrentEthAccount(getApplication());
            return new EthTxLoader(code);
        } else if (Coins.SOL.coinId().equals(coinId)) {
            String code = Utilities.getCurrentSolAccount(getApplication());
            return new SolTxLoader(code);
        } else if (Coins.NEAR.coinId().equals(coinId)) {
            String code = Utilities.getCurrentNearAccount(getApplication());
            return new NearTxLoader(code);
        } else {
            return new GeneralTxLoader(coinId);
        }
    }


}
