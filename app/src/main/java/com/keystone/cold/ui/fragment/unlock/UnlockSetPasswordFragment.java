package com.keystone.cold.ui.fragment.unlock;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.ui.fragment.setpassword.BaseSetPasswordFragement;
import com.keystone.cold.util.Keyboard;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

public class UnlockSetPasswordFragment extends BaseSetPasswordFragement {
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
        mnemonic = bundle != null ? bundle.getString(BaseSetPasswordFragement.MNEMONIC) : null;
        slip39MasterSeed = bundle != null ? bundle.getString(BaseSetPasswordFragement.SLIP39_SEED) : null;
        slip39Id = bundle != null ? bundle.getInt(BaseSetPasswordFragement.SLIP39_ID) : 0;
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
