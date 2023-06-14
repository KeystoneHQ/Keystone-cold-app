package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CardanoTxViewModel;

public class CardanoConfirmTransactionFragment extends ConfirmTransactionFragment<CardanoTransaction, CardanoTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(CardanoTxViewModel.class);
        viewModel.parseTxData(requireArguments());
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
        mBinding.toolbar.setNavigationOnClickListener((v) -> navigateUp());
        mBinding.sign.setOnClickListener(v -> {
            handleSign();
        });
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.ADA.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
