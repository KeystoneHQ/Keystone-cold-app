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

import com.keystone.cold.R;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.databinding.SettingFragmentBinding;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;

public class SettingFragment extends BaseFragment<SettingFragmentBinding> {

    public static final String TAG = "SettingFragment";

    @Override
    protected int setView() {
        return R.layout.setting_fragment;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        if (FeatureFlags.ENABLE_REMOVE_WALLET_MODE) {
            mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        } else {
            mBinding.toolbar.setNavigationOnClickListener(((com.keystone.cold.ui.MainActivity) mActivity)::toggleDrawer);
        }
        mBinding.toolbar.setTitle("");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
