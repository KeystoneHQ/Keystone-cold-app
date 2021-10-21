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

package com.keystone.cold.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.internal.app.LocalePicker;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AttackWarningBinding;
import com.keystone.cold.setting.LanguageHelper;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.util.DataCleaner;

import java.util.Objects;
import java.util.concurrent.Executors;

import static com.keystone.cold.selfcheck.SecurityCheck.CODE_FW_GET_STATUS_FAILED;
import static com.keystone.cold.selfcheck.SecurityCheck.CODE_FW_IN_BOOTMODE;
import static com.keystone.cold.selfcheck.SecurityCheck.CODE_STATUS_MIS_MATCH;
import static com.keystone.cold.selfcheck.SecurityCheck.CODE_STATUS_RUNTIME_INVALID;
import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.removeAllFingerprint;
import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.reset;

public class AttackWarningFragment extends BaseFragment<AttackWarningBinding> {

    public static AttackWarningFragment newInstance(Bundle data){
        AttackWarningFragment fragment = new AttackWarningFragment();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.attack_warning;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        mBinding.hint.setText(getString(R.string.attack_warning,
                 formatErrorCode(data)));
        int firmware = data.getInt("firmware");

        if (firmware == CODE_FW_IN_BOOTMODE) {
            mBinding.text1.setText(R.string.update_failed);
            mBinding.hint.setText(getString(R.string.contact_keystone_service, formatErrorCode(data)));
        } else if(firmware == CODE_FW_GET_STATUS_FAILED) {
            mBinding.text1.setText(R.string.opration_failed);
            mBinding.hint.setText(getString(R.string.reboot_hint,formatErrorCode(data)));
        } else if ((firmware & 0xff00) == CODE_STATUS_MIS_MATCH || (firmware & 0xff00) == CODE_STATUS_RUNTIME_INVALID) {
            mBinding.text1.setText(R.string.abnormal_state_title);
            mBinding.hint.setText(getString(R.string.abnormal_state_hint,formatErrorCode(data)));
        }

        if (firmware == CODE_FW_GET_STATUS_FAILED) {
            mBinding.powerOff.setText(R.string.reboot);
            mBinding.powerOff.setOnClickListener(v -> reboot());
        } else if ((firmware & 0xff00) == CODE_STATUS_MIS_MATCH) {
            mBinding.powerOff.setText(R.string.factory_reset);
            mBinding.powerOff.setOnClickListener(v -> reset(mActivity));
        } else {
            mBinding.powerOff.setText(R.string.clear_and_power_off);
            mBinding.powerOff.setOnClickListener(v -> handleAttack(mActivity));
        }
        mBinding.serialno.setText(getString(R.string.serialno, SystemProperties.get("persist.sys.serialno")));
        mBinding.icon.setOnClickListener(new AboutFragment.ExportLogHandler(mActivity, Executors.newSingleThreadExecutor()));
    }

    private void reboot() {
        new Thread("reboot") {
            @Override
            public void run() {
                IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                try {
                    pm.reboot(false, null, false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void handleAttack(AppCompatActivity activity) {
        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(activity.getSupportFragmentManager()), "");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DataCleaner.cleanApplicationData(activity);
                removeAllFingerprint(activity);
                LocalePicker.updateLocale(LanguageHelper.defaultLocale);
            } catch (Exception ignore){
            }finally {
                DataCleaner.cleanApplicationData(activity);
                powerOff();
            }
        });
    }

    private void powerOff() {
        Intent i = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        i.putExtra("android.intent.extra.KEY_CONFIRM", false);
        i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(i);
    }

    private String formatErrorCode(Bundle data) {
        int firmware = data.getInt("firmware");
        int system = data.getInt("system");
        int signature = data.getInt("signature");
        return "0x" + String.format("%08x", firmware << 16 | system << 8 | signature);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
