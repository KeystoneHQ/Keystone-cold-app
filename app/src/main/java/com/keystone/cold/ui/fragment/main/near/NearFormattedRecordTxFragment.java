package com.keystone.cold.ui.fragment.main.near;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.NearTxDetailBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.NearTxViewModel;

public class NearFormattedRecordTxFragment extends BaseFragment<NearTxDetailBinding> {


    private NearTxViewModel viewModel;


    public static Fragment newInstance(@NonNull Bundle bundle) {
        NearFormattedRecordTxFragment fragment = new NearFormattedRecordTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.near_tx_detail;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(getParentFragment()).get(NearTxViewModel.class);
        viewModel.getNearTxLiveData().observe(this, nearTx -> {
            if (nearTx != null) {
                mBinding.network.setText(nearTx.getNetWork());
                mBinding.from.setText(nearTx.getSignerId());
                mBinding.to.setText(nearTx.getReceiverId());
                mBinding.actions.setData(nearTx);

                mBinding.qrcode.qrcode.setData(nearTx.getUr());
                mBinding.qr.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getNearTxLiveData().removeObservers(this);
    }
}

