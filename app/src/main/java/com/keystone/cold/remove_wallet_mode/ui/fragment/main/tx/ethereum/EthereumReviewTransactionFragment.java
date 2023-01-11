package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ReviewTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;

public class EthereumReviewTransactionFragment extends ReviewTransactionFragment<EthereumTxViewModel> {
    private MutableLiveData<EthereumTransaction> transaction;

    @Override
    protected void initViewModel() {
        Bundle data = requireArguments();
        viewModel = ViewModelProviders.of(this).get(EthereumTxViewModel.class);
        transaction = viewModel.getObservableEthTx();
        String txId = data.getString(BundleKeys.TX_ID_KEY);
        viewModel.generateSignedTransaction(txId);
        viewModel.parseTxException().observe(this, this::handleParseException);
    }

    private void handleParseException(BaseException ex) {
        if (ex != null) {
            ex.printStackTrace();
            alertException(ex, () -> {
                popBackStack(R.id.assetFragment, false);
            });
            viewModel.parseTxException().setValue(null);
        }
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[2];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), EthereumTransactionDetailFragment.newInstance(requireArguments(), transaction));
        layoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {

    }

    @Override
    protected void onSignSuccess() {

    }
}
