package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ripple;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.RippleTxViewModel;

import org.json.JSONObject;

public class RippleReviewTransactionFragment extends ReviewTransactionFragment<JSONObject, RippleTxViewModel> {
    @Override
    protected void initViewModel() {
        Bundle bundle = requireArguments();
        String txId = bundle.getString(BundleKeys.TX_ID_KEY);
        viewModel = ViewModelProviders.of(this).get(RippleTxViewModel.class);
        viewModel.parseExistingTransaction(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), RippleTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return configs;
    }

    @Override
    protected void setupView() {
    }
}
