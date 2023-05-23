package com.keystone.cold.remove_wallet_mode.helper;

import android.os.Bundle;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ReviewDestinationDetector {

    private static final String TAG = "ReviewDetector";

    private static Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    public static Destination detect(Tx tx) {

        String txId = tx.getTxId();
        String coinId = tx.getCoinId();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.TX_ID_KEY, txId);
        // Keystone tx(GenericETHTx);
        if (tx.getSignId() == null) {
            return new Destination(R.id.action_to_keystoneReviewTransactionFragment, bundle);
        } else if (uuidPattern.matcher(tx.getSignId()).find()) {
            // Keystone tx(TxEntity);
            return new Destination(R.id.action_to_keystoneReviewTransactionFragment, bundle);
        }
        if (Coins.SUI.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_suiReviewTransactionFragment, bundle);
        } else if (Coins.APTOS.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_aptosReviewTransactionFragment, bundle);
        } else if (Coins.ETH.coinId().equals(coinId)) {
            return detectEth(tx);
        } else if (Coins.SOL.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_solanaReviewTransactionFragment, bundle);
        } else if (Coins.BTC.coinId().equals(coinId) || Coins.BTC_NATIVE_SEGWIT.coinId().equals(coinId) || Coins.BTC_LEGACY.coinId().equals(coinId) || Coins.BTC_CORE_WALLET.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_bitcoinReviewTransactionFragment, bundle);
        } else if (Coins.DOT.coinId().equals(coinId) || Coins.KSM.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_substrateReviewTransactionFragment, bundle);
        } else if (Coins.NEAR.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_nearReviewTransactionFragment, bundle);
        } else if (Coins.AR.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_arweaveReviewTransactionFragment, bundle);
        } else if (Coins.isCosmosFamilyByCoinId(coinId)) {
            if (tx instanceof GenericETHTxEntity) {
                return detectEth(tx);
            }
            return new Destination(R.id.action_to_cosmosReviewTransactionFragment, bundle);
        } else if (Coins.XRP.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_rippleReviewTransactionFragment, bundle);
        } else if (Coins.ADA.coinId().equals(coinId)) {
            return new Destination(R.id.action_to_cardanoReviewTransactionFragment, bundle);
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
                // maybe not needed anymore
                new JSONObject(signedHex);
                return new Destination(R.id.action_to_ethereumReviewTransactionFragment, bundle);
            } catch (JSONException e) {
                switch (ethTxEntity.getTxType()) {
                    case 0x00:
                    case 0x02:
                        return new Destination(R.id.action_to_ethereumReviewTransactionFragment, bundle);
                }
            }
        }
        return null;
    }


}
