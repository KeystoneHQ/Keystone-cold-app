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

package com.keystone.cold.ui.fragment.whitelist;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.databinding.WhiteListBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import static com.keystone.cold.ui.fragment.Constants.KEY_NAV_ID;
import static com.keystone.cold.ui.fragment.Constants.KEY_TITLE;

public class WhiteListFragment extends BaseFragment<WhiteListBinding> {

    @Override
    protected int setView() {
        return R.layout.white_list;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.verifyMnemonic.setOnClickListener(this::toVerifyMnemonic);
        if (FeatureFlags.ENABLE_SHARDING_MNEMONIC) {
            mBinding.verifySharding.setOnClickListener(this::toVerifySharding);
        } else {
            mBinding.verifySharding.setVisibility(View.GONE);
        }
    }

    private void toVerifyMnemonic(View view) {
        Bundle data = new Bundle();
        data.putString(KEY_TITLE, mBinding.verifyMnemonic.getText().toString());
        data.putInt(KEY_NAV_ID, R.id.action_to_manageWhiteList);
        navigate(R.id.white_list_to_verify_mnemonic, data);
    }

    private void toVerifySharding(View view) {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
