package com.keystone.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupWelcomeBinding;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

public class WelcomeFragment extends SetupVaultBaseFragment<SetupWelcomeBinding> {
    @Override
    protected int setView() {
        return R.layout.setup_welcome;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> {
            viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_WEB_AUTH);
            navigate(R.id.action_to_webAuthFragment);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
