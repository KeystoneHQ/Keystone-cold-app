package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ripple;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.RippleTxViewModel;

import org.json.JSONObject;

public class RippleConfirmTransactionFragment extends ConfirmTransactionFragment<JSONObject, RippleTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(RippleTxViewModel.class);
        viewModel.parseTxData(requireArguments());
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), RippleTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return configs;
    }

    @Override
    protected void setupView() {
        mBinding.sign.setOnClickListener((v) -> {
            super.handleSign();
        });
    }

    @Override
    protected void onSignSuccess() {
        String signedTxHex = viewModel.getSignedTxHex();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signedTxHex);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.XRP.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
