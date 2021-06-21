/*
 *
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
 *
 */

package com.keystone.cold.ui.fragment.setup.sharding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SelectBottomSheetBinding;
import com.keystone.cold.databinding.ShardingSettingBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.stream.IntStream;

public class ShardingSettingFragment extends BaseFragment<ShardingSettingBinding> implements NumberPickerCallback {

    private State state = State.STATE_NONE;
    private int total = 5;
    private int threshold = 3;

    @Override
    protected int setView() {
        return R.layout.sharding_setting;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.total.setOnClickListener(v -> selectTotalNumber());
        mBinding.threshold.setOnClickListener(v -> selectThresholdNumber());
        mBinding.confirm.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putInt("total", total);
            data.putInt("threshold", threshold);
            navigate(R.id.action_to_shardingGuideFragment, data);
        });
        updateUI();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void updateUI() {
        mBinding.total.setRemindText(String.valueOf(total));
        mBinding.threshold.setRemindText(String.valueOf(threshold));
    }

    private void selectTotalNumber() {
        state = State.STATE1;
        String[] displayValue = IntStream.range(1, 16)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        showSelector(displayValue, total, 2, 16, getString(R.string.total_sharding_count));
    }

    private void selectThresholdNumber() {
        state = State.STATE2;
        String[] displayValue = IntStream.range(1, total)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        showSelector(displayValue, threshold, 2, total, getString(R.string.sharding_threshold));
    }

    private void showSelector(String[] displayValue, int value, int min, int max, String title) {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        SelectBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.select_bottom_sheet, null, false);
        binding.setValue(value);
        binding.title.setText(title);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(min);
        binding.picker.setMaxValue(max);
        binding.picker.setValue(value);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal));
        binding.confirm.setText(R.string.confirm);
        binding.confirm.setOnClickListener(v -> {
            onValueSet(binding.picker.getValue());
            dialog.dismiss();

        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onValueSet(int value) {
        if (state == State.STATE1) {
            total = value;
        } else if (state == State.STATE2) {
            threshold = value;
        }
        threshold = Math.min(threshold, total);
        state = State.STATE_NONE;
        updateUI();
    }

    enum State {
        STATE1,
        STATE2,
        STATE_NONE
    }
}
