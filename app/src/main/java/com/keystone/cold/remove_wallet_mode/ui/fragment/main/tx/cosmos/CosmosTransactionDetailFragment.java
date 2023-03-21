package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentCosmosTxBinding;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.CosmosTx;
import com.keystone.cold.ui.fragment.BaseFragment;

public class CosmosTransactionDetailFragment extends BaseFragment<FragmentCosmosTxBinding> {

    public static Fragment newInstance(@NonNull Bundle bundle, LiveData<CosmosTx> cosmosTxLiveData) {
        CosmosTransactionDetailFragment fragment = new CosmosTransactionDetailFragment();
        fragment.setArguments(bundle);
        fragment.cosmosTxLiveData = cosmosTxLiveData;
        return fragment;
    }


    private LiveData<CosmosTx> cosmosTxLiveData;


    @Override
    protected int setView() {
        return R.layout.fragment_cosmos_tx;
    }

    @Override
    protected void init(View view) {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if (cosmosTxLiveData != null) {
            cosmosTxLiveData.observe(this, this::updateUI);
        }
    }

    private void updateUI(CosmosTx cosmosTx) {
        if (cosmosTx != null) {
            mBinding.setCoinCode(cosmosTx.getCoinCode());
            mBinding.setCheckInfoTitle(cosmosTx.getCoinName());
            mBinding.tvChainId.setText(cosmosTx.getChainId());
            if (cosmosTx.getFee() != null) {
                String denom = cosmosTx.getFee().getAmountDenom() == null ? "" : cosmosTx.getFee().getAmountDenom().toUpperCase();
                mBinding.tvFee.setText(cosmosTx.getFee().getAmountValue() + " " + denom);
                mBinding.tvGasLimit.setText(cosmosTx.getFee().getGas());
            } else {
                mBinding.llFee.setVisibility(View.GONE);
                mBinding.llGas.setVisibility(View.GONE);
            }
            mBinding.cmMessages.setData(cosmosTx.getMsgs());
            if (!TextUtils.isEmpty(cosmosTx.getUr())) {
                mBinding.qrcode.qrcode.setData(cosmosTx.getUr());
                mBinding.qr.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (cosmosTxLiveData != null) {
            cosmosTxLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }
}
