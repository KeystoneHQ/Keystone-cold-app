package com.keystone.cold.ui.fragment.main.cosmos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CosmosTxDetailBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.cosmos.model.CosmosTx;
import com.keystone.cold.viewmodel.tx.CosmosTxViewModel;



public class CosmosFormattedTxFragment extends BaseFragment<CosmosTxDetailBinding> {

    private CosmosTxViewModel viewModel;
    private boolean isFromRecord = false;


    public static Fragment newInstance(@NonNull Bundle bundle) {
        CosmosFormattedTxFragment fragment = new CosmosFormattedTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(@NonNull Bundle bundle, boolean isFromRecord) {
        CosmosFormattedTxFragment fragment = new CosmosFormattedTxFragment();
        fragment.isFromRecord = isFromRecord;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.cosmos_tx_detail;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(getParentFragment()).get(CosmosTxViewModel.class);

        if (isFromRecord) {


        } else {
            viewModel.getCosmosTxLiveData().observe(this, cosmosTx -> {
                if (cosmosTx != null) {
                    updateUI(cosmosTx);
                }
            });
        }
    }

    private void updateUI(CosmosTx cosmosTx) {
        mBinding.tvChainId.setText(cosmosTx.getChainId());
        if (cosmosTx.getFee() != null) {
            mBinding.tvFee.setText(cosmosTx.getFee().getAmountValue() + " " + cosmosTx.getFee().getAmountDenom().toUpperCase());
            mBinding.tvGasLimit.setText(cosmosTx.getFee().getGas());
        }
        mBinding.cmMessages.setData(cosmosTx.getMsgs());
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }


}