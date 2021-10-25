package com.keystone.cold.ui.fragment.setting;

import android.os.Bundle;

import com.keystone.cold.ui.fragment.setpassword.BaseSetPasswordFragement;

public class ChangePasswordFragment extends BaseSetPasswordFragement {

    private String currentPassword;

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        Bundle bundle = getArguments();
        currentPassword = bundle != null ? bundle.getString(BaseSetPasswordFragement.PASSWORD) : null;
    }

    @Override
    public void setPasswordAndJump() {
        changePasswordCallable(currentPassword);
        navigateUp();
    }
}
