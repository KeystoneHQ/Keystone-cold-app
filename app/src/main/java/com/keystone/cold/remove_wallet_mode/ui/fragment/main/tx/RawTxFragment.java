package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentRawTxRemoveWalletModeBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BaseTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

public class RawTxFragment extends BaseFragment<FragmentRawTxRemoveWalletModeBinding> {

    private BaseTxViewModel viewModel;

    public static Fragment newInstance(Bundle bundle) {
        RawTxFragment fragment = new RawTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_raw_tx_remove_wallet_mode;
    }

    @Override
    protected void init(View view) {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle bundle = requireArguments();
        String coinCode = bundle.getString(BundleKeys.COIN_CODE_KEY);
        viewModel = BaseTxViewModel.getTxViewModel(coinCode, getParentFragment());
        if (viewModel != null) {
            viewModel.getRawFormatTx().observe(this, rawTx -> {
                if (rawTx != null) {
                    mBinding.rawTx.setText(rawTx);
                } else {
                    mBinding.rawTx.setText(R.string.decode_failed_hint);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.getRawFormatTx().removeObservers(this);
        }
    }
}
