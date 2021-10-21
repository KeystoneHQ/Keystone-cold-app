package com.keystone.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupWelcomeBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class WelcomeFragment extends BaseFragment<SetupWelcomeBinding> {
    @Override
    protected int setView() {
        return R.layout.setup_welcome;
    }

    @Override
    protected void init(View view) {
        mBinding.complete.setOnClickListener(v -> {
            navigate(R.id.action_to_webAuthFragment);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
