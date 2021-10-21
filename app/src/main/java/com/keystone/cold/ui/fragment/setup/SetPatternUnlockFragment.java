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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.ModalBinding;
import com.keystone.cold.databinding.SetPatternUnlockBinding;
import com.keystone.cold.ui.UnlockActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.HashUtil;

import org.spongycastle.util.encoders.Hex;

import java.util.List;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

public class SetPatternUnlockFragment extends BaseFragment<SetPatternUnlockBinding> {

    private static final int STAGE_1 = 1;
    private static final int STAGE_2 = 2;
    private int stage = STAGE_1;
    private String patternStr;

    private boolean inSetupProcess;
    private final PatternLockViewListener mListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {

        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if (stage == STAGE_1) {
                if (pattern.size() < 4) {
                    ModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                            R.layout.modal, null, false);
                    binding.icon.setImageResource(R.drawable.circle_info);
                    binding.text.setText(R.string.dots_not_enough);
                    ModalDialog dialog = ModalDialog.newInstance();
                    dialog.setBinding(binding);
                    dialog.show(mActivity.getSupportFragmentManager(), "");
                    new Handler().postDelayed(() -> {
                        dialog.dismiss();
                        mBinding.lock.clearPattern();
                    }, 1000);
                } else {
                    patternStr = PatternLockUtils.patternToString(mBinding.lock, pattern);
                    stage = STAGE_2;
                    mBinding.hint1.setText(R.string.input_pattern_again);
                    mBinding.hint1.setTextColor(mActivity.getColor(R.color.colorAccent));

                    mBinding.lock.clearPattern();
                }
            } else if (stage == STAGE_2) {
                if (!patternStr.equals(PatternLockUtils.patternToString(mBinding.lock, pattern))) {
                    mBinding.hint1.setText(R.string.two_pattern_not_match);
                    mBinding.hint1.setTextColor(mActivity.getColor(R.color.red));
                    stage = STAGE_1;
                    mBinding.lock.clearPattern();
                } else {
                    Utilities.setPattern(mActivity,
                            HashUtil.twiceSha256(Hex.decode(PatternLockUtils.patternToSha1(mBinding.lock, pattern)))
                    );

                    ModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                            R.layout.modal, null, false);
                    binding.icon.setImageResource(R.drawable.circle_positive);
                    binding.icon.setImageTintList(ColorStateList.valueOf(mActivity.getColor(R.color.colorAccent)));
                    binding.text.setText(R.string.set_pattern_complete);
                    ModalDialog dialog = ModalDialog.newInstance();
                    dialog.setBinding(binding);
                    dialog.show(mActivity.getSupportFragmentManager(), "");
                    new Handler().postDelayed(() -> {
                        dialog.dismiss();

                        if (mActivity instanceof UnlockActivity) {
                            Utilities.setPatternRetryTimes(mActivity, 0);
                            mActivity.finish();
                        } else {
                            navigateUp();
                        }
                    }, 1000);
                }
            }
        }

        @Override
        public void onCleared() {

        }
    };

    @Override
    protected int setView() {
        return R.layout.set_pattern_unlock;
    }

    @Override
    protected void init(View view) {

        Bundle data = getArguments();
        inSetupProcess = data != null && data.getBoolean(IS_SETUP_VAULT);
        if (inSetupProcess) {
            mBinding.toolbar.setVisibility(View.GONE);
            mBinding.divider.setVisibility(View.GONE);
        } else {
            mBinding.step.setVisibility(View.GONE);
            mBinding.skip.setVisibility(View.GONE);
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        }
        mBinding.lock.addPatternLockListener(mListener);
        mBinding.skip.setOnClickListener(this::skip);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void skip(View view) {
        if (mActivity instanceof UnlockActivity) {
            mActivity.finish();
        }
    }
}
