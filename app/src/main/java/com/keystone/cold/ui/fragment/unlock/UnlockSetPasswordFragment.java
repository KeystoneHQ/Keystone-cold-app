package com.keystone.cold.ui.fragment.unlock;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.Utilities;
import com.keystone.cold.ui.fragment.SetPasswordBaseFragement;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

public class UnlockSetPasswordFragment extends SetPasswordBaseFragement {
    private String mnemonic; // mnemonic to reset password
    private String slip39MasterSeed; // masterSeed to reset password
    private int slip39Id;

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            navigateUp();
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        Bundle bundle = getArguments();
        mnemonic = bundle != null ? bundle.getString(SetPasswordBaseFragement.MNEMONIC) : null;
        slip39MasterSeed = bundle != null ? bundle.getString(SetPasswordBaseFragement.SLIP39_SEED) : null;
        slip39Id = bundle != null ? bundle.getInt(SetPasswordBaseFragement.SLIP39_ID) : 0;
    }

    @Override
    public void setPasswordAndJump() {
        boolean hasSetupFinished = Utilities.getVaultCreateStep(mActivity).equals(SetupVaultViewModel.VAULT_CREATE_STEP_DONE);
        resetPassword(mnemonic, slip39MasterSeed, slip39Id);
        if (!hasSetupFinished) {
            Utilities.markPasswordSet(mActivity);
        }
        Utilities.setPatternRetryTimes(mActivity, 0);
        mActivity.finish();
    }
}
