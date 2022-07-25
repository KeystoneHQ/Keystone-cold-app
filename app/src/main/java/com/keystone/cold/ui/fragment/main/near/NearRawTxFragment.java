package com.keystone.cold.ui.fragment.main.near;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolRawTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.NearTxViewModel;

import org.json.JSONException;

public class NearRawTxFragment extends BaseFragment<FragmentSolRawTxBinding> {

    private NearTxViewModel viewModel;


    public static Fragment newInstance(@NonNull Bundle bundle) {
        NearRawTxFragment fragment = new NearRawTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sol_raw_tx;
    }

    @Override
    protected void init(View view) {

        viewModel = ViewModelProviders.of(getParentFragment()).get(NearTxViewModel.class);
        viewModel.getParseMessageJsonLiveData().observe(this, jsonArray -> {
            if (jsonArray != null) {
                try {
                    mBinding.rawTx.setText(jsonArray.toString(2));
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                mBinding.rawTx.setText(R.string.decode_failed_hint);
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getParseMessageJsonLiveData().removeObservers(this);
    }
}
