package com.keystone.cold.ui.fragment.main;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.SetPasswordBaseFragement;
import com.keystone.cold.util.Keyboard;

public class MainSetPasswordFragment extends SetPasswordBaseFragement {

    private String mnemonic; // mnemonic to reset password
    private String slip39MasterSeed; // masterSeed to reset password
    private int slip39Id;

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.pwd1);
            popBackStack(R.id.preImportFragment, false);
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
        resetPassword(mnemonic, slip39MasterSeed, slip39Id);
        popBackStack(R.id.preImportFragment, true);
    }
}
