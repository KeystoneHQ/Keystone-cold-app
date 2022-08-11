package com.keystone.cold.ui.fragment.main.solana;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolOverviewBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SolInstructionFragment extends BaseFragment<FragmentSolOverviewBinding> {

    private SolTxViewModel viewModel;
    private final boolean isOverview;
    private final boolean isRaw;

    public SolInstructionFragment(boolean isOverview, boolean isRaw) {
        this.isOverview = isOverview;
        this.isRaw = isRaw;
    }

    static Fragment newInstance(boolean isOverview, boolean isRaw) {
        SolInstructionFragment fragment = new SolInstructionFragment(isOverview, isRaw);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sol_overview;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(getParentFragment()).get(SolTxViewModel.class);
        viewModel.getParseMessageJsonLiveData().observe(this, jsonObject -> {
            if (jsonObject != null) {
                if (jsonObject.toString().equals(new JSONObject().toString())) {
                    return;
                }
                try {
                    if(isRaw) {
                        mBinding.rawTx.setText(jsonObject.toString(2));
                        mBinding.rawTx.setVisibility(View.VISIBLE);
                    }
                    else {
                        InstructionAdapter adapter = new InstructionAdapter(getContext(), this.isOverview);
                        JSONArray instructions = jsonObject.getJSONArray("instructions");
                        int length = instructions.length();
                        List<JSONObject> list = new ArrayList<>();
                        for (int i = 0; i < length; i++) {
                            list.add(instructions.getJSONObject(i));
                        }
                        adapter.setItems(list);
                        mBinding.instructions.setAdapter(adapter);
                        mBinding.instructions.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                mBinding.rawTx.setText(R.string.decode_failed_hint);
                mBinding.rawTx.setVisibility(View.VISIBLE);
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
        viewModel.getParseMessageJsonLiveData().setValue(new JSONObject());
    }

}
