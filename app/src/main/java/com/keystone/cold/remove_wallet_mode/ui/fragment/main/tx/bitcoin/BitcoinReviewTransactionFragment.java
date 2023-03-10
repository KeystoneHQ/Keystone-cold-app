package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;

public class BitcoinReviewTransactionFragment extends ReviewTransactionFragment<PSBT, BitcoinTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(BitcoinTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[1];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), BitcoinTransactionDetailsFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.parseExistingTransaction(txId);
    }
}
