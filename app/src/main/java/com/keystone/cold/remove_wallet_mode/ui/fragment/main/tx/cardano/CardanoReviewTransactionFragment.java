package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CardanoTxViewModel;

public class CardanoReviewTransactionFragment extends ReviewTransactionFragment<CardanoTransaction, CardanoTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(CardanoTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        return new TabLayoutConfig[0];
    }

    @Override
    protected void setupView() {

    }

    @Override
    protected void onSignSuccess() {

    }
}
