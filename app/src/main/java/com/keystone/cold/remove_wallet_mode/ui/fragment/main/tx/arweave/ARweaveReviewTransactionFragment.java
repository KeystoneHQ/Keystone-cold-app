package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.ARweaveTxViewModel;

public class ARweaveReviewTransactionFragment extends ReviewTransactionFragment<ArweaveTransaction, ARweaveTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ARweaveTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[1];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), ARweaveTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.parseExistingTransaction(txId);
        viewModel.getObservableException().observe(this, this::handleParseException);
    }
}
