package com.keystone.cold.ui.fragment.main.solana;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolRawTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

import org.json.JSONException;

public class SolRawTxFragment extends BaseFragment<FragmentSolRawTxBinding> {

    private SolTxViewModel viewModel;


    static Fragment newInstance(@NonNull Bundle bundle) {
        SolRawTxFragment fragment = new SolRawTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sol_raw_tx;
    }

    @Override
    protected void init(View view) {

        viewModel = ViewModelProviders.of(mActivity).get(SolTxViewModel.class);
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
            viewModel.getParseMessageJsonLiveData().removeObservers(this);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
