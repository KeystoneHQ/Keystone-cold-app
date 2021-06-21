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

package com.keystone.cold.ui.fragment.setting;

import android.graphics.Color;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.navigation.fragment.NavHostFragment;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.FingerprintManageBinding;
import com.keystone.cold.databinding.InputModalBinding;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.fingerprint.RemovalListener;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.Objects;

import static com.keystone.cold.callables.FingerprintPolicyCallable.OFF;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_PASSPHRASE;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.callables.FingerprintPolicyCallable.WRITE;
import static com.keystone.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;

public class FingerprintManageFragment extends BaseFragment<FingerprintManageBinding> {

    private Fingerprint fingerprint;
    private FingerprintKit fpKit;
    private final ObservableField<String> input = new ObservableField<>();
    private String fingerprintName;
    private ModalDialog dialog;
    private String password;

    @Override
    protected int setView() {
        return R.layout.fingerprint_manage;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();

        fpKit = new FingerprintKit(mActivity);
        fingerprint = data.getParcelable("fingerprint");
        fingerprintName = fingerprint.getName().toString();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbarTitle.setText(fingerprint.getName());
        mBinding.rename.title.setText(R.string.rename_fingerprint);
        mBinding.rename.item.setOnClickListener(v -> rename());

        mBinding.remove.title.setText(R.string.delete_fingerprint);
        mBinding.remove.title.setTextColor(Color.parseColor("#F0264E"));
        mBinding.remove.item.setOnClickListener(v -> remove());
        input.set(fingerprint.getName().toString());
        password = data.getString(PASSWORD);
    }

    private void remove() {
        dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        binding.title.setText(R.string.delete_fingerprint);
        binding.subTitle.setText(getString(R.string.confirm_delete) + fingerprintName);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setBackgroundColor(Color.parseColor("#DF1E44"));
        binding.confirm.setText(R.string.confirm_delete);
        binding.confirm.setOnClickListener(v -> {
            fpKit.removeFingerprint(fingerprint, new RemovalListener() {
                @Override
                public void onSuccess() {
                    if (!fpKit.hasEnrolledFingerprint()) {
                        Utilities.setFingerprintUnlockEnable(mActivity, false);
                        new FingerprintPolicyCallable(password, WRITE, TYPE_PASSPHRASE, OFF).call();
                        new FingerprintPolicyCallable(password, WRITE, TYPE_SIGN_TX, OFF).call();
                        NavHostFragment.findNavController(FingerprintManageFragment.this)
                                .popBackStack(R.id.settingFragment, false);
                    } else {
                        navigateUp();
                    }

                }

                @Override
                public void onError(int errMsgId, String errString) {

                }
            });
            dialog.dismiss();
            mBinding.toolbarTitle.setText(input.get());
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void rename() {
        dialog = new ModalDialog();
        InputModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.input_modal, null, false);
        binding.title.setText(R.string.fingerprint_rename_subtitle);
        binding.setInput(input);
        binding.inputBox.setSelectAllOnFocus(true);
        binding.inputBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setOnClickListener(v -> {
            fpKit.renameFingerprint(fingerprint, input.get());
            dialog.dismiss();
            mBinding.toolbarTitle.setText(input.get());
            fingerprintName = input.get();
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
