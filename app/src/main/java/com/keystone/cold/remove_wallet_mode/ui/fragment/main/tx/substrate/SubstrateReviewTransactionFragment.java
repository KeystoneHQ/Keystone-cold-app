package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SubstrateTxViewModel;

public class SubstrateReviewTransactionFragment extends ReviewTransactionFragment<SubstrateTransaction, SubstrateTxViewModel> {
    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel = ViewModelProviders.of(this).get(SubstrateTxViewModel.class);
        viewModel.reset();
        viewModel.parseExistingTransaction(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[1];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), SubstrateTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {
    }
}
