package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentKeyRequestBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class KeyRequestFragment extends BaseFragment<FragmentKeyRequestBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_key_request;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();

        mBinding.toolbar.setNavigationOnClickListener((v) -> {
            navigateUp();
        });

        mBinding.confirm.setOnClickListener((v) -> {
            navigate(R.id.action_to_scanner);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
