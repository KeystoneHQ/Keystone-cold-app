package com.keystone.cold.remove_wallet_mode.helper;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.util.Log;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import org.json.JSONException;
import org.json.JSONObject;

public class ReviewDestinationDetector {

    private static final String TAG = "ReviewDetector";


    public static Destination detect(Tx tx) {

        String txId = tx.getTxId();
        String coinId = tx.getCoinId();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.TX_ID_KEY, txId);
        if (Coins.APTOS.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_aptosReviewTransactionFragment, bundle);
        } else if (Coins.ETH.coinId().equals(coinId)) {
            return detectEth(tx);
        } else if (Coins.SOL.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_solanaReviewTransactionFragment, bundle);
        } else if (Coins.BTC.coinId().equals(coinId) || Coins.BTC_NATIVE_SEGWIT.coinId().equals(coinId) || Coins.BTC_LEGACY.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_bitcoinReviewTransactionFragment, bundle);
        } else if (Coins.DOT.coinId().equals(coinId) || Coins.KSM.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_substrateReviewTransactionFragment, bundle);
        } else if (Coins.NEAR.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_nearReviewTransactionFragment, bundle);
        }
        return null;
    }

    private static Destination detectEth(Tx tx) {
        if (tx instanceof GenericETHTxEntity) {
            GenericETHTxEntity ethTxEntity = (GenericETHTxEntity) tx;
            String signedHex = ethTxEntity.getSignedHex();
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.TX_ID_KEY, ethTxEntity.getTxId());
            try {
                new JSONObject(signedHex);
                return new Destination(R.id.action_to_ethereumReviewTransactionFragment, bundle);
            } catch (JSONException e) {
                switch (ethTxEntity.getTxType()) {
                    case 0x00:
                        return new Destination(R.id.action_to_ethereumReviewTransactionFragment, bundle);
                    case 0x02:
                        Log.i(TAG, "navigate: jump to ethFeeMarketTxFragment");
                        return new Destination(R.id.action_to_ethereumReviewTransactionFragment, bundle);
                }
            }
        }
        return null;
    }


}
