package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.keystone;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.KeystoneTxViewModel;

public class KeystoneReviewTransactionFragment extends ReviewTransactionFragment<TxEntity, KeystoneTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(KeystoneTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[1];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), KeystoneTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {
        Bundle data = requireArguments();
        viewModel.parseExistingTransaction(data.getString(BundleKeys.TX_ID_KEY));
    }
}
