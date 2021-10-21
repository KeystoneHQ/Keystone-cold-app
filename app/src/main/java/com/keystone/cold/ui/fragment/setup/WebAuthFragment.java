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

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.WebAuthBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

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
                    initScanResult();
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


    private void initScanResult() {
        ViewModelProviders.of(mActivity).get(ScannerViewModel.class)
                .setState(new ScannerState(Collections.singletonList(ScanResultTypes.UR_BYTES)) {
                    @Override
                    public void handleScanResult(ScanResult result) throws Exception {
                        if (handleWebAuth(result)) return;
                        throw new UnknowQrCodeException("Unknow qrcode");
                    }

                    @Override
                    public boolean handleException(Exception e) {
                        e.printStackTrace();
                        mFragment.alert(getString(R.string.invalid_data), getString(R.string.unsupported_qrcode));
                        return true;
                    }

                    private boolean handleWebAuth(ScanResult result) throws JSONException {
                        JSONObject object = new JSONObject(new String((byte[]) result.resolve(), StandardCharsets.UTF_8));
                        JSONObject webAuth = object.optJSONObject("data");
                        if (TextUtils.equals(Objects.requireNonNull(webAuth).optString("type"), "webAuth")) {
                            String webAuthData = webAuth.getString("data");
                            Bundle bundle = new Bundle();
                            bundle.putString(WEB_AUTH_DATA, webAuthData);
                            bundle.putBoolean(IS_SETUP_VAULT, true);
                            mFragment.navigate(R.id.action_to_webAuthResultFragment, bundle);
                            return true;
                        }
                        return false;
                    }
                });
    }
}
