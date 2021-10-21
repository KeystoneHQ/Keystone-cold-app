/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupVaultBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.OneTimePasswordManager;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.keystone.cold.ui.fragment.setup.SetPasswordFragment.SHOULD_POP_BACK;

import androidx.navigation.Navigation;

public class SetupVaultFragment extends BaseFragment<SetupVaultBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_vault;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = getArguments();
        initToolbarUI(bundle);
        mBinding.importVault.setOnClickListener(this::importVault);
        mBinding.createVault.setOnClickListener(this::createVault);
    }

    private void initToolbarUI(Bundle bundle) {
        if (bundle != null && bundle.getBoolean(IS_SETUP_VAULT)) {
            mBinding.step.setVisibility(View.VISIBLE);
            mBinding.toolbar.setVisibility(View.GONE);
            mBinding.divider.setVisibility(View.GONE);
        } else {
            mBinding.step.setVisibility(View.GONE);
            mBinding.toolbar.setVisibility(View.VISIBLE);
            mBinding.toolbar.setNavigationOnClickListener(v -> mActivity.onBackPressed());
        }
    }

    private void createVault(View view) {
        AuthenticateModal.show(mActivity, mActivity.getString(R.string.password_modal_title),
                "",
                password -> {
                    OneTimePasswordManager.getInstance().setPasswordHash(password.password);
                    navigate(R.id.action_to_tabletQrcodeFragment);
                },
                () -> {
                    Bundle data = new Bundle();
                    data.putBoolean(IS_SETUP_VAULT, true);
                    data.putBoolean(SHOULD_POP_BACK, true);
                    Navigation.findNavController(mActivity, R.id.nav_host_fragment)
                            .navigate(R.id.global_action_to_setPasswordFragment, data);
                });

    }

    private void importVault(View view) {
        AuthenticateModal.show(mActivity, mActivity.getString(R.string.password_modal_title),
                "",
                password -> {
                    OneTimePasswordManager.getInstance().setPasswordHash(password.password);
                    Bundle data = new Bundle();
                    data.putString(ACTION, PreImportFragment.ACTION_IMPORT);
                    navigate(R.id.action_to_preImportFragment, data);
                },
                () -> {
                    Bundle data = new Bundle();
                    data.putBoolean(IS_SETUP_VAULT, true);
                    data.putBoolean(SHOULD_POP_BACK, true);
                    Navigation.findNavController(mActivity, R.id.nav_host_fragment)
                            .navigate(R.id.global_action_to_setPasswordFragment, data);
                });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
