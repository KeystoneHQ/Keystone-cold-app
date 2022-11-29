package com.keystone.cold.ui.fragment.main.cosmos;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentAptosRawTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.CosmosTxViewModel;

import org.json.JSONException;

public class CosmosRawTxFragment extends BaseFragment<FragmentAptosRawTxBinding> {

    private CosmosTxViewModel viewModel;
    private boolean isFromRecord = false;

    public static Fragment newInstance(@NonNull Bundle bundle) {
        CosmosRawTxFragment fragment = new CosmosRawTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(@NonNull Bundle bundle, boolean isFromRecord) {
        CosmosRawTxFragment fragment = new CosmosRawTxFragment();
        fragment.setArguments(bundle);
        fragment.isFromRecord = isFromRecord;
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_aptos_raw_tx;
    }

    @Override
    protected void init(View view) {

        viewModel = ViewModelProviders.of(getParentFragment()).get(CosmosTxViewModel.class);
        if (isFromRecord) {
            viewModel.getCosmosTxDataMutableLiveData().observe(this, aptosTxData -> {
                if (aptosTxData != null) {
                    mBinding.rawTx.setText(aptosTxData.getParsedMessage());
                }
            });
        } else {
            viewModel.getParseMessageJsonLiveData().observe(this, jsonObject -> {
                if (jsonObject != null) {
                    try {
                        mBinding.rawTx.setText(jsonObject.toString(2));
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    mBinding.rawTx.setText(R.string.decode_failed_hint);
                }
            });
        }


    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isFromRecord) {
            viewModel.getCosmosTxDataMutableLiveData().removeObservers(this);
        } else {
            viewModel.getParseMessageJsonLiveData().removeObservers(this);
        }
    }
}
