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

import android.app.Activity;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ModalWithTwoButtonBinding;
import com.keystone.cold.sdcard.SdcardFormatHelper;
import com.keystone.cold.setting.VibratorHelper;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.preference.SwitchPreference;

import java.util.List;
import java.util.Objects;

import static android.content.Context.STORAGE_SERVICE;
import static com.keystone.cold.Utilities.SHARED_PREFERENCES_KEY;
import static com.keystone.cold.ui.fragment.Constants.KEY_TITLE;

public class SystemPreferenceFragment extends PreferenceFragmentCompat {

    public static final String SETTING_LANGUAGE = "setting_language";
    public static final String SETTING_VIBRATOR = "setting_vibrator";
    private static final String SETTING_BRIGHTNESS = "setting_brightness";
    static final String SETTING_SCREEN_OFF_TIME = "setting_screen_off_time";
    private static final String SETTING_FORMAT = "setting_format_sd_card";
    private Activity mActivity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.system_preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        SwitchPreference switchPreference = findPreference(SETTING_VIBRATOR);
        if (switchPreference != null) {
            try {
                switchPreference.setChecked(Settings.System.getInt(mActivity.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED)==1);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Bundle data = new Bundle();
        switch (preference.getKey()) {
            case SETTING_LANGUAGE:
                data.putInt(KEY_TITLE, R.string.setting_language);
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_languagePreferenceFragment, data);
                break;
            case SETTING_VIBRATOR:
                SwitchPreference switchPreference = (SwitchPreference) preference;
                final boolean newValue = !switchPreference.isChecked();
                if (switchPreference.callChangeListener(newValue)) {
                    switchPreference.setChecked(newValue);
                }
                Settings.System.putInt(mActivity.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, newValue ? 1 : 0);
                if (newValue) {
                    VibratorHelper.vibrate(Objects.requireNonNull(mActivity));
                }
                break;
            case SETTING_BRIGHTNESS:
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_brightnessSettingFragment);
                break;
            case SETTING_SCREEN_OFF_TIME:
                data.putInt(KEY_TITLE, R.string.setting_screen_off_time);
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_screenOffPreferenceFragment, data);
                break;
            case SETTING_FORMAT:
                formatSdcard();
                break;

        }
        return super.onPreferenceTreeClick(preference);
    }

    private void formatSdcard() {
        StorageManager storageManager = (StorageManager) mActivity.getSystemService(STORAGE_SERVICE);
        List<DiskInfo> diskInfoList = storageManager.getDisks();
        if (diskInfoList.size() == 0) {
            ModalDialog.showCommonModal((AppCompatActivity) getActivity(),
                    getString(R.string.no_sdcard),
                    getString(R.string.please_insert_sdcard),
                    getString(R.string.confirm),
                    null);
        } else {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button, null,false);
            binding.title.setText(getString(R.string.confirm_format));
            binding.subTitle.setText(getString(R.string.format_sd_card_hint));
            binding.subTitle.setGravity(Gravity.START);
            binding.left.setText(R.string.cancel);
            binding.left.setOnClickListener(v -> dialog.dismiss());
            binding.right.setText(R.string.confirm);
            binding.right.setOnClickListener(v -> {
                dialog.dismiss();
                new SdcardFormatHelper().showFormatProgress((AppCompatActivity) getActivity(), storageManager, diskInfoList.get(0));
            });
            dialog.setBinding(binding);
            dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),"");
        }
    }

}
