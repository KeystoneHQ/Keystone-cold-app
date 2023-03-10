package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.ARweaveTxViewModel;

public class ARweaveConfirmTransactionFragment extends ConfirmTransactionFragment<ArweaveTransaction, ARweaveTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ARweaveTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[1];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), ARweaveTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {
        viewModel.parseTxData(requireArguments());
        mBinding.sign.setOnClickListener((v) -> handleSign());
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.AR.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
