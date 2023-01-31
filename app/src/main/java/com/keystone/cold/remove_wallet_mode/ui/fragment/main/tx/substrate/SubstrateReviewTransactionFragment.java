package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SubstrateTxViewModel;

public class SubstrateReviewTransactionFragment extends ReviewTransactionFragment<SubstrateTxViewModel> {
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
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), SubstrateTransactionDetailFragment.newInstance(requireArguments(), viewModel.getTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return configs;
    }

    @Override
    protected void setupView() {
        viewModel.getException().observe(this, (e) -> {
            if (e == null) return;
            handleParseException(e);
        });
    }

    private void handleParseException(BaseException ex) {
        if (ex != null) {
            ex.printStackTrace();
            alertException(ex, () -> {
                popBackStack(R.id.myAssetsFragment, false);
            });
            viewModel.getException().setValue(null);
        }
    }
}
