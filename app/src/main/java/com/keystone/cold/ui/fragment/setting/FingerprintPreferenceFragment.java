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

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.ui.preference.SwitchPreference;

import java.util.Objects;

import static com.keystone.cold.Utilities.SHARED_PREFERENCES_KEY;
import static com.keystone.cold.callables.FingerprintPolicyCallable.OFF;
import static com.keystone.cold.callables.FingerprintPolicyCallable.ON;
import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_PASSPHRASE;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.callables.FingerprintPolicyCallable.WRITE;
import static com.keystone.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;

public class FingerprintPreferenceFragment extends PreferenceFragmentCompat {

    public static final String FINGERPRINT_UNLOCK = "fingerprint_unlock";
    public static final String FINGERPRINT_SIGN = "fingerprint_sign";
    public static final String FINGERPRINT_PASSPHRASE = "fingerprint_passphrase";

    private SwitchPreference fingerprintUnlock;
    private SwitchPreference fingerprintSign;
    private SwitchPreference fingerprintPassphrase;
    private String password;
    private Activity mActivity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        password = Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getArguments())
                .getString(PASSWORD);
        mActivity = getActivity();
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.fingerprint_preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintUnlock = findPreference(FINGERPRINT_UNLOCK);
        if (fingerprintUnlock != null) {
            fingerprintUnlock.setChecked(Utilities.isFingerprintUnlockEnable(mActivity));
        }

        fingerprintSign = findPreference(FINGERPRINT_SIGN);
        if (fingerprintSign != null) {
            fingerprintSign.setChecked(new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call());
        }

        fingerprintPassphrase = findPreference(FINGERPRINT_PASSPHRASE);
        if (fingerprintPassphrase != null) {
            fingerprintPassphrase.setChecked(new FingerprintPolicyCallable(READ, TYPE_PASSPHRASE).call());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean isChecked = !((SwitchPreference)preference).isChecked();
        switch (preference.getKey()) {
            case FINGERPRINT_UNLOCK:
                fingerprintUnlock.setChecked(isChecked);
                Utilities.setFingerprintUnlockEnable(mActivity, isChecked);
                break;
            case FINGERPRINT_SIGN:
                fingerprintSign.setChecked(isChecked);
                new FingerprintPolicyCallable(password, WRITE, TYPE_SIGN_TX, isChecked ? ON : OFF).call();
                break;
            case FINGERPRINT_PASSPHRASE:
                fingerprintPassphrase.setChecked(isChecked);
                new FingerprintPolicyCallable(password, WRITE, TYPE_PASSPHRASE, isChecked ? ON : OFF).call();
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }

}
