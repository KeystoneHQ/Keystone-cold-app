package com.keystone.cold.ui.fragment.setup;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.ui.fragment.setpassword.BaseSetPasswordFragement;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

public class SetUpSetPasswordFragment extends BaseSetPasswordFragement {

    private boolean shouldPopBack;

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setVisibility(View.GONE);
        mBinding.divider.setVisibility(View.GONE);
        mBinding.step.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_SET_PASSWORD);
        Utilities.clearPasswordSet(mActivity);
        Bundle bundle = getArguments();
        shouldPopBack = bundle != null && bundle.getBoolean(BaseSetPasswordFragement.SHOULD_POP_BACK);
    }

    @Override
    public void setPasswordAndJump() {
        resetPassword(null, null, 0);
        Utilities.markPasswordSet(mActivity);
        if (shouldPopBack) {
            navigateUp();
        } else {
            Bundle data = new Bundle();
            data.putBoolean(IS_SETUP_VAULT, true);
            viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_FIRMWARE_UPGRADE);
            navigate(R.id.action_to_firmwareUpgradeFragment, data);
        }
    }
}
