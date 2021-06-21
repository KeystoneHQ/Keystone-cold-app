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

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.keystone.cold.R;
import com.keystone.cold.databinding.BrightnessSettingBinding;
import com.keystone.cold.setting.BrightnessHelper;
import com.keystone.cold.ui.fragment.BaseFragment;

public class BrightnessSettingFragment extends BaseFragment<BrightnessSettingBinding> {
    @Override
    protected int setView() {
        return R.layout.brightness_setting;
    }

    @Override
    protected void init(View view) {
        BrightnessHelper.setManualMode(mActivity);
        int brightness = BrightnessHelper.getBrightness(mActivity);
        mBinding.seekbar.setProgress(brightness);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BrightnessHelper.setBrightness(mActivity, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
