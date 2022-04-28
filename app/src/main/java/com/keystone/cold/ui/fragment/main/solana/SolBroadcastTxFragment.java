package com.keystone.cold.ui.fragment.main.solana;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

public class SolBroadcastTxFragment extends BroadcastTxFragment {

    private String signatureURString;

    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }


    @Override
    protected void init(View view) {

        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        Bundle data = requireArguments();
        String txId = data.getString(KEY_TXID);
        signatureURString = data.getString(KEY_SIGNATURE_UR);
        mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());

        if (!TextUtils.isEmpty(txId)) {
            ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                    .loadTx(txId).observe(this, txEntity -> {
                if (txEntity != null) {
                    mBinding.setCoinCode(txEntity.getCoinCode());
                    this.txEntity = txEntity;
                    refreshUI();
                }
            });
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        mBinding.broadcastHint.setText(R.string.sync_with_metamask);
    }


    @Override
    public String getSignedTxData() {
        return signatureURString;
    }
}
