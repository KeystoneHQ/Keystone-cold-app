package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SolanaTxViewModel;

import org.json.JSONObject;

public class SolanaReviewTransactionFragment extends ReviewTransactionFragment<JSONObject, SolanaTxViewModel> {

    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel = ViewModelProviders.of(this).get(SolanaTxViewModel.class);
        viewModel.parseTransactionFromRecord(txId);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[4];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), SolanaTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction(), "overview"));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.details), SolanaTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction(), "details"));
        tabLayoutConfigs[2] = new TabLayoutConfig(getString(R.string.raw), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        tabLayoutConfigs[3] = new TabLayoutConfig(getString(R.string.qr), SolSignResultFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return tabLayoutConfigs;
    }

    @Override
    protected void setupView() {

    }

    @Override
    protected void onSignSuccess() {

    }
}
