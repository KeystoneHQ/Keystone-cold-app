package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentRawTxRemoveWalletModeBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class RawTxFragment extends BaseFragment<FragmentRawTxRemoveWalletModeBinding> {

    private LiveData<String> rawFormatTx;

    public static Fragment newInstance(Bundle bundle, LiveData<String> rawFormatTx) {
        RawTxFragment fragment = new RawTxFragment();
        fragment.setArguments(bundle);
        fragment.rawFormatTx = rawFormatTx;
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
        if (rawFormatTx != null) {
            rawFormatTx.observe(this, rawTx -> {
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
        if (rawFormatTx != null) {
            rawFormatTx.removeObservers(this);
        }
    }
}
