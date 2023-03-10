package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTx;

public class AptosConfirmTransactionFragment extends ConfirmTransactionFragment<AptosTx, AptosTxViewModel> {


    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AptosTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[2];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), AptosFormattedTxFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return tabLayoutConfigs;
    }

    @Override
    protected void setupView() {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> {
            handleSign();
        });
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.APTOS.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        viewModel.parseTxData(requireArguments());
    }
}
