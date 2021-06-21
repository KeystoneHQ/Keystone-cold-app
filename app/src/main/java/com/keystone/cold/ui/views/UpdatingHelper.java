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

package com.keystone.cold.ui.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.keystone.cold.R;
import com.keystone.cold.databinding.UpdateHintModalBinding;
import com.keystone.cold.databinding.UpdatingBinding;
import com.keystone.cold.encryptioncore.utils.Preconditions;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.update.data.UpdateManifest;
import com.keystone.cold.viewmodel.UpdatingViewModel;

import java.util.Objects;

import static android.content.Context.BATTERY_SERVICE;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class UpdatingHelper implements OnBatteryChangeListener {

    private final UpdatingViewModel updatingViewModel;
    private final AppCompatActivity mActivity;
    private final boolean proactive;
    private int batteryPercent = -1;
    private MutableLiveData<UpdateManifest> manifestLiveData = new MutableLiveData<>();

    public UpdatingHelper(AppCompatActivity activity, boolean proactive) {
        mActivity = activity;
        this.proactive = proactive;
        updatingViewModel = ViewModelProviders.of(mActivity).get(UpdatingViewModel.class);
        registerBroadcastReceiver(activity);
    }

    public UpdatingHelper(AppCompatActivity activity) {
        this(activity,false);
    }

    public MutableLiveData<UpdateManifest> getUpdateManifest() {
        if (updatingViewModel.getUpdateManifest().getValue() != null) {
            UpdateManifest manifest = updatingViewModel.getUpdateManifest().getValue();
            manifestLiveData.setValue(manifest);
        } else {
            updatingViewModel.getUpdateManifest().observe(mActivity, updateManifest -> {
                if (updateManifest != null) {
                    manifestLiveData.setValue(updateManifest);
                }
            });
        }
        return manifestLiveData;
    }

    public void onUpdatingDetect(UpdateManifest manifest) {
        BatteryManager manager = (BatteryManager) mActivity.getSystemService(BATTERY_SERVICE);
        int percent = batteryPercent != -1 ? batteryPercent :
                Objects.requireNonNull(manager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        ModalDialog dialog = new ModalDialog();

        UpdateHintModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.update_hint_modal, null, false);

        dialog.setBinding(binding);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.footer.setVisibility(proactive ? View.GONE : View.VISIBLE);
        binding.subTitle.setText(mActivity.getString(R.string.new_version_hint_message,
                getDisplayVersion(manifest)));
        binding.sha256.setText("\nsha256:\n" + manifest.sha256);
        if (percent < UpdatingViewModel.MIN_BATTERY_FOR_UPDATE) {
            String batterHint = mActivity.getString(R.string.update_alert_boot_low_battery_message,
                    UpdatingViewModel.MIN_BATTERY_FOR_UPDATE + "%", percent +"%");
            binding.subTitle.setText(batterHint);
            binding.sha256.setVisibility(View.GONE);
            binding.checkbox.setVisibility(View.GONE);
            binding.agreeButton.setChecked(true);
            binding.confirm.setText(R.string.know);
            binding.footer.setVisibility(View.GONE);
            binding.confirm.setOnClickListener(v -> dialog.dismiss());
        } else {
            binding.confirm.setOnClickListener(v -> {
                dialog.dismiss();
                AuthenticateModal.show(mActivity,
                        mActivity.getString(R.string.password_modal_title),
                        "",
                        password->{
                            updatingViewModel.doUpdate(password.password);
                            subscribeUpdateState();
                        },
                         () -> {
                            Bundle data = new Bundle();
                            data.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
                            Navigation.findNavController(mActivity, R.id.nav_host_fragment)
                                    .navigate(R.id.action_to_preImportFragment, data);
                        }
                        );
            });

        }
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private String getDisplayVersion(UpdateManifest manifest) {
        if (manifest == null) {
            return "";
        } else if(manifest.app != null) {
            return manifest.app.displayVersion;
        } else if(manifest.system != null) {
            return manifest.system.displayVersion;
        } else {
            return "";
        }
    }
    private void subscribeUpdateState() {
        ModalDialog dialog = new ModalDialog();
        UpdatingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.updating, null, false);
        dialog.setBinding(binding);
        updatingViewModel.getUpdatingState().observe(mActivity, updatingState -> {
            switch (updatingState) {
                case UPDATING:
                    dialog.show(mActivity.getSupportFragmentManager(), "");
                    break;
                case UPDATING_SUCCESS:
                case UPDATING_FAILED:
                    if (dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                        dialog.dismiss();
                    }
                    new Handler().postDelayed(this::restartApplication, 2000);
                    break;
                default:
                    break;
            }
        });
    }

    private void restartApplication() {
        final Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(mActivity.getPackageName());
        Objects.requireNonNull(intent).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onBatteryChange(int percentage) {
        batteryPercent = percentage;
    }

    @NonNull
    private BroadcastReceiver registerBroadcastReceiver(@NonNull Context context) {
        Preconditions.checkNotNull(context);
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final BroadcastReceiver receiver = new BatteryView.BatteryReceiver(this);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
