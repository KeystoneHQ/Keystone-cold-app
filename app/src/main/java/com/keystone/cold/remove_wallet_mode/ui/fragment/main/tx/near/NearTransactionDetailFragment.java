package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentNearTxBinding;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.NearTx;
import com.keystone.cold.ui.fragment.BaseFragment;

public class NearTransactionDetailFragment extends BaseFragment<FragmentNearTxBinding> {

    private LiveData<NearTx> nearTxLiveData;

    public static Fragment newInstance(@NonNull Bundle bundle, LiveData<NearTx> nearTxLiveData) {
        NearTransactionDetailFragment fragment = new NearTransactionDetailFragment();
        fragment.setArguments(bundle);
        fragment.nearTxLiveData = nearTxLiveData;
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_near_tx;
    }

    @Override
    protected void init(View view) {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mBinding.setCoinCode(Coins.NEAR.coinCode());
        mBinding.setCheckInfoTitle(Coins.NEAR.coinName());
        if (nearTxLiveData != null) {
            nearTxLiveData.observe(this, nearTx -> {
                if (nearTx != null) {
                    mBinding.setCheckInfoTitle(nearTx.getNetWork());
                    mBinding.from.setText(nearTx.getSignerId());
                    mBinding.to.setText(nearTx.getReceiverId());
                    mBinding.actions.setData(nearTx);
                    if (!TextUtils.isEmpty(nearTx.getUr())) {
                        mBinding.qrcode.qrcode.setData(nearTx.getUr());
                        mBinding.qr.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        if (nearTxLiveData != null) {
            nearTxLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }
}
