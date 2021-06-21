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

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SelectMnemonicCountBinding;

import java.util.Objects;

import static com.keystone.cold.ui.fragment.Constants.KEY_TITLE;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.IS_SHARDING;

public class SelectMnomenicCountFragment extends SetupVaultBaseFragment<SelectMnemonicCountBinding> {

    private boolean checkMnemonic;
    private boolean isSharding;
    private String action;
    private Bundle bundle;

    @Override
    protected int setView() {
        return R.layout.select_mnemonic_count;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.setViewModel(viewModel);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.next.setOnClickListener(this::next);
        bundle = requireArguments();
        action = bundle.getString(ACTION);

        isSharding = bundle.getBoolean(IS_SHARDING);
        checkMnemonic = PreImportFragment.ACTION_CHECK.equals(action);

        if (isSharding) {
            mBinding.normalMnemonicCount.setVisibility(View.GONE);
        } else {
            mBinding.shardingMnemonicCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSharding) {
            viewModel.resetSharding();
            viewModel.setMnemonicCount(20);
        } else {
            viewModel.setMnemonicCount(24);
        }
    }

    private void next(View view) {
        if (checkMnemonic) {
            bundle.putString(KEY_TITLE, getString(R.string.check_mnemonic));
            navigate(R.id.action_to_verifyMnemonic, bundle);
        } else if(action.equals(PreImportFragment.ACTION_RESET_PWD)) {
            bundle.putString(KEY_TITLE, getString(R.string.input_mnemonic));
            navigate(R.id.action_to_verifyMnemonic, bundle);
        } else {
            navigate(R.id.action_to_mnemonicInputFragment, getArguments());
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
