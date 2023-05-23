package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.sui;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos.AptosFormattedTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SuiTxViewModel;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTx;
import com.keystone.cold.ui.fragment.main.sui.model.SuiTx;

public class SuiReviewTransactionFragment extends ReviewTransactionFragment<SuiTx, SuiTxViewModel> {

    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel = ViewModelProviders.of(this).get(SuiTxViewModel.class);
        viewModel.parseTransactionFromRecord(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[2];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), SuiFormattedTxFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return tabLayoutConfigs;
    }

    @Override
    protected void setupView() {

    }

    @Override
    protected void onSignSuccess() {

    }
}
