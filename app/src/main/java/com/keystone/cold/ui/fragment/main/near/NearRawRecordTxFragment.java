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
import org.json.JSONObject;

public class NearRawRecordTxFragment extends BaseFragment<FragmentSolRawTxBinding> {

    public static final String RAW_DATA_KEY = "raw_data_key";

    private NearTxViewModel viewModel;


    public static Fragment newInstance(@NonNull Bundle bundle) {
        NearRawRecordTxFragment fragment = new NearRawRecordTxFragment();
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
        viewModel.getNearTxLiveData().observe(this, nearTx -> {
            if (nearTx != null) {
                String rawData = nearTx.getRawData();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(rawData);
                    mBinding.rawTx.setText(jsonObject.toString(2));
                    return;
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
            mBinding.rawTx.setText(R.string.decode_failed_hint);
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
