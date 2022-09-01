package com.keystone.cold.ui.fragment.main.aptos;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentAptosRawTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.AptosViewModel;

import org.json.JSONException;

public class AptosRawTxFragment extends BaseFragment<FragmentAptosRawTxBinding> {

    private AptosViewModel viewModel;

    public static Fragment newInstance(@NonNull Bundle bundle) {
        AptosRawTxFragment fragment = new AptosRawTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    protected int setView() {
        return R.layout.fragment_aptos_raw_tx;
    }

    @Override
    protected void init(View view) {

        viewModel = ViewModelProviders.of(getParentFragment()).get(AptosViewModel.class);
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
