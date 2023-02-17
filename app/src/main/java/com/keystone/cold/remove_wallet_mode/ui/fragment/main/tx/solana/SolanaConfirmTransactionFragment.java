package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SolanaTxViewModel;

import org.json.JSONObject;

public class SolanaConfirmTransactionFragment extends ConfirmTransactionFragment<JSONObject, SolanaTxViewModel> {

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SolanaTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[3];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), SolanaTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction(), "overview"));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.details), SolanaTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction(), "details"));
        tabLayoutConfigs[2] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
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
    protected void initData(Bundle savedInstanceState) {
        viewModel.parseTxData(requireArguments());
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.SOL.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }


}
