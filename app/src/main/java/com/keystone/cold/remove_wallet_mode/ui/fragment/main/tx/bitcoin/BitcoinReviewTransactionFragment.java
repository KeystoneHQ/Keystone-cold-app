package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;
import com.keystone.cold.ui.modal.ProgressModalDialog;

import java.util.Objects;

public class BitcoinReviewTransactionFragment extends ReviewTransactionFragment<PSBT, BitcoinTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(BitcoinTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), BitcoinTransactionDetailsFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return configs;
    }

    @Override
    protected void setupView() {
        Bundle data = requireArguments();
        viewModel.getObservableException().observe(this, this::handleParseException);
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.parseExistingTransaction(txId);
    }
}
