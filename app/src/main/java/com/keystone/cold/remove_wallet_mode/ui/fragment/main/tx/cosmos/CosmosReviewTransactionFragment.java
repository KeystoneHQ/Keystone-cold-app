package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.CosmosTx;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CosmosTxViewModel;

public class CosmosReviewTransactionFragment extends ReviewTransactionFragment<CosmosTx, CosmosTxViewModel> {

    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel = ViewModelProviders.of(this).get(CosmosTxViewModel.class);
        viewModel.parseTransactionFromRecord(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[2];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), CosmosTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return tabLayoutConfigs;
    }

    @Override
    protected void setupView() {

    }
}
