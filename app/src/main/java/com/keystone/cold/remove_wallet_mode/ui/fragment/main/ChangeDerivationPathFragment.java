package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;


import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentChangeDerivationPathBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.ChangePathViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.List;


public class ChangeDerivationPathFragment extends BaseFragment<FragmentChangeDerivationPathBinding> {
    private String coinId;
    private String selectCode;
    private ChangePathViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.fragment_change_derivation_path;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        coinId = data.getString(KEY_COIN_ID);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.ivConfirm.setOnClickListener(v -> save());
        mBinding.pathPatternView.setOnItemClick(code -> selectCode = code);
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(ChangePathViewModel.class);
        subscribeUI(viewModel.getPathPattern(coinId));
    }

    private void subscribeUI(LiveData<List<PathPatternItem>> pathPatternItemsLiveData) {
        pathPatternItemsLiveData.observe(this, pathPatternItems -> {
            mBinding.pathPatternView.setData(pathPatternItems);
            pathPatternItemsLiveData.removeObservers(this);
        });
    }

    private void save() {
        viewModel.save(coinId, selectCode);
        navigateUp();
    }
}
