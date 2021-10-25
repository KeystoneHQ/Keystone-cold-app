package com.keystone.cold.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.ui.fragment.SetPasswordBaseFragement;

public class ChangePasswordFragment extends SetPasswordBaseFragement {

    private String currentPassword;

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
        currentPassword = bundle != null ? bundle.getString(SetPasswordBaseFragement.PASSWORD) : null;
    }

    @Override
    public void setPasswordAndJump() {
        changePasswordCallable(currentPassword);
        navigateUp();
    }
}
