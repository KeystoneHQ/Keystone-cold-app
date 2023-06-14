package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CardanoTxViewModel;

public class CardanoReviewTransactionFragment extends ReviewTransactionFragment<CardanoTransaction, CardanoTxViewModel> {
    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        viewModel = ViewModelProviders.of(this).get(CardanoTxViewModel.class);
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.parseExistingTransaction(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), CardanoTransactionOverviewFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.details), CardanoTransactionDetailsFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {

    }
}
