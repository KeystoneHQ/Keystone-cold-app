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

package com.keystone.cold.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.ui.common.FullScreenActivity;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import java.util.Objects;

import static com.keystone.cold.ui.fragment.Constants.IS_FORCE;
import static com.keystone.cold.ui.fragment.PasswordLockFragment.MAX_PWD_RETRY_TIMES;
import static com.keystone.cold.ui.fragment.PatternLockFragment.MAX_PATTERN_RETRY_TIMES;

public class UnlockActivity extends FullScreenActivity {

    private boolean isFingerprintLockEnable;
    private TextView statusHint;

    private static final int FAIL_COUNT_TO_PATTERN_UNLOCK = 2;

    private FingerprintIdentify mFingerprintIdentify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlock);

        isFingerprintLockEnable = Utilities.isFingerprintUnlockEnable(this);
        if (isFingerprintLockEnable) {
            mFingerprintIdentify = new FingerprintIdentify(getApplicationContext());
            mFingerprintIdentify.setSupportAndroidL(true);
            mFingerprintIdentify.init();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNavController();
    }

    private void setupNavController() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavInflater inflater = Objects.requireNonNull(navHostFragment).getNavController().getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.nav_graph_unlock);

        int startDestination = R.id.passwordLockFragment;
        Bundle data = new Bundle();

        if (isFingerprintLockEnable) {
            startDestination = R.id.fingerprintLockFragment;
        } else if (Utilities.isPatternUnlock(this)
                && Utilities.getPatternRetryTimes(this) < MAX_PATTERN_RETRY_TIMES) {
            startDestination = R.id.patternLockFragment;
        } else if (Utilities.getPasswordRetryTimes(this) < MAX_PWD_RETRY_TIMES) {
            data.putBoolean(IS_FORCE, true);
            startDestination = R.id.passwordLockFragment;
        }

        graph.setStartDestination(startDestination);
        navHostFragment.getNavController().setGraph(graph, data);
    }

    @Override
    public void onBackPressed() {

    }

    public void startFingerprintVerify() {
        mFingerprintIdentify.startIdentify(FAIL_COUNT_TO_PATTERN_UNLOCK, new BaseFingerprint.IdentifyListener() {
            @Override
            public void onSucceed() {
                FingerprintKit.verifyPassword(UnlockActivity.this);
                finish();
            }

            @Override
            public void onNotMatch(int availableTimes) {

            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                onFingerVerifyFailed();
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                onFingerVerifyFailed();
            }
        });
    }

    private void onFingerVerifyFailed() {
        if (Utilities.isPatternUnlock(UnlockActivity.this)
                && Utilities.getPatternRetryTimes(UnlockActivity.this) < MAX_PATTERN_RETRY_TIMES) {
            Navigation.findNavController(Objects.requireNonNull(statusHint))
                    .navigate(R.id.action_fingerprint_to_patternLockFragment);
        } else {
            Bundle data = new Bundle();
            data.putBoolean(IS_FORCE, true);
            Navigation.findNavController(Objects.requireNonNull(statusHint))
                    .navigate(R.id.action_fingerprint_to_passwordLockFragment, data);
        }
    }


    public void setStatusHint(TextView verifyHint) {
        statusHint = verifyHint;
    }

    public void cancelIdentify() {
        mFingerprintIdentify.cancelIdentify();
    }

    public void startIdentify() {
        if (isFingerprintLockEnable) {
            startFingerprintVerify();
        }
    }
}
