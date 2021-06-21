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

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.PatternLockFragmentBinding;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.setting.VibratorHelper;
import com.keystone.cold.util.HashUtil;

import org.spongycastle.util.encoders.Hex;

import java.util.List;

import static com.keystone.cold.ui.fragment.Constants.IS_FORCE;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION_RESET_PWD;

public class PatternLockFragment extends BaseFragment<PatternLockFragmentBinding> {

    public static final int MAX_PATTERN_RETRY_TIMES = 12;
    public static final String TAG = "PatternLockFragment";
    public int attemptTimes;

    private final View.OnClickListener gotoPasswordUnlock =
            v -> {
                Bundle data = new Bundle();
                data.putBoolean(IS_FORCE, false);
                data.putString(ACTION, ACTION_RESET_PWD);
                navigate(R.id.action_to_passwordLockFragment, data);
            };

    @Override
    protected int setView() {
        return R.layout.pattern_lock_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.switchToPassword.setOnClickListener(gotoPasswordUnlock);
        mBinding.lockView.setTactileFeedbackEnabled(false);
        mBinding.lockView.setInputEnabled(true);
        attemptTimes = Utilities.getPatternRetryTimes(mActivity);

        mBinding.lockView.addPatternLockListener(new PatternLockViewListener() {

            @Override
            public void onStarted() {
                mBinding.lockView.setInStealthMode(true);
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (pattern.size() < 4) {
                    mBinding.lockView.clearPattern();
                    return;
                }

                String patternHash = PatternLockUtils.patternToSha1(mBinding.lockView, pattern);
                patternHash = HashUtil.twiceSha256(Hex.decode(patternHash));
                if (Utilities.verifyPatternUnlock(mActivity, patternHash)) {
                    mBinding.lockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Utilities.setPatternRetryTimes(mActivity, 0);
                    FingerprintKit.verifyPassword(mActivity);
                    mActivity.finish();
                } else {
                    VibratorHelper.vibrate(mActivity);
                    mBinding.lockView.setInStealthMode(false);
                    mBinding.lockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    attemptTimes++;
                    Utilities.setPatternRetryTimes(mActivity, attemptTimes);
                    if (attemptTimes > 0) {
                        mBinding.hint.setText(R.string.wrong_pattern_hint);
                    }

                    new Handler().postDelayed(() -> mBinding.lockView.clearPattern(), 1000);
                }
                if (attemptTimes >= MAX_PATTERN_RETRY_TIMES) {
                    Bundle data = new Bundle();
                    data.putBoolean(IS_FORCE, true);
                    data.putString(ACTION, ACTION_RESET_PWD);
                    navigate(R.id.action_to_passwordLockFragment, data);
                }
            }

            @Override
            public void onCleared() {
                Log.d(getClass().getName(), "Pattern has been cleared");
            }

        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
