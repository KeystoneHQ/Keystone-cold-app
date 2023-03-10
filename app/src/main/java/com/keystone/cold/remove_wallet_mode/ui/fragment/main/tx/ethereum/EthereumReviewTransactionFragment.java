package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;

public class EthereumReviewTransactionFragment extends ReviewTransactionFragment<EthereumTransaction, EthereumTxViewModel> {

    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        viewModel = ViewModelProviders.of(this).get(EthereumTxViewModel.class);
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.generateSignedTransaction(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[1];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), EthereumTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {

    }
}
