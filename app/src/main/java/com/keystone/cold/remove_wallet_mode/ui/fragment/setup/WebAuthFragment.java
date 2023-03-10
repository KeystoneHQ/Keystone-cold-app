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

package com.keystone.cold.remove_wallet_mode.ui.fragment.setup;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.keystone.cold.R;
import com.keystone.cold.databinding.WebAuthBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.SetupVaultViewModel;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

public class WebAuthFragment extends SetupVaultBaseFragment<WebAuthBinding> {
    @Override
    protected int setView() {
        return R.layout.web_auth;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.scan.setOnClickListener(this::scanVerify);
        mBinding.skip.setOnClickListener(this::skip);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    void scanVerify(View v) {
        AndPermission.with(this)
                .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    navigate(R.id.action_to_scanner);
                })
                .onDenied(permissions -> {
                    Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(mActivity, R.string.scan_permission_denied, Toast.LENGTH_LONG).show();
                }).start();
    }

    void skip(View v) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_SETUP_VAULT, true);
        viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_SET_PASSWORD);
        navigate(R.id.action_webAuthFragment_to_setPasswordFragment, bundle);
    }

}
