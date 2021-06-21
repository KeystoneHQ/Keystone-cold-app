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

package com.keystone.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.PreImportShardingBinding;

import java.util.Objects;

public class PreImportFragment extends SetupVaultBaseFragment<PreImportShardingBinding> {
    public static final String ACTION = "action";
    public static final String IS_SHARDING = "is_sharding";
    public static final String ACTION_IMPORT = "import_mnemonic";
    public static final String ACTION_CHECK = "check_mnemonic";
    public static final String ACTION_RESET_PWD = "reset_pwd";

    @Override
    protected int setView() {
        return R.layout.pre_import_sharding;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle bundle = requireArguments();
        String action = bundle.getString(ACTION);
        refreshUI(action);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.importNormalMnemonic.setOnClickListener(v -> {
            bundle.putBoolean(IS_SHARDING, false);
            navigate(R.id.action_to_selectMnomenicCountFragment, bundle);
        });
        mBinding.importShardingMnemonic.setOnClickListener(v -> {
            bundle.putBoolean(IS_SHARDING, true);
            navigate(R.id.action_to_selectMnomenicCountFragment, bundle);
        });
    }

    private void refreshUI(String action) {
        String title = "";
        String text = "";
        String buttonText1 = "";
        String buttonText2 = "";
        switch (action) {
            case ACTION_CHECK:
                title = getString(R.string.check_mnemonic);
                text = getString(R.string.choose_check_type);
                buttonText1 = getString(R.string.check_bip39_phrase);
                buttonText2 = getString(R.string.check_slip39_phrase);
                break;
            case ACTION_IMPORT:
                title = getString(R.string.import_vault);
                text = getString(R.string.choose_import_type);
                buttonText1 = getString(R.string.import_bip39_phrase);
                buttonText2 = getString(R.string.import_slip39_phrase);
                break;
            case ACTION_RESET_PWD:
                title = getString(R.string.reset_password);
                text = getString(R.string.reset_password_by_phrase);
                buttonText1 = getString(R.string.enter_bip39_phrase);
                buttonText2 = getString(R.string.enter_slip39_phrase);
                break;
        }

        mBinding.toolbarTitle.setText(title);
        mBinding.hint.setText(text);
        mBinding.importNormalMnemonic.setText(buttonText1);
        mBinding.importShardingMnemonic.setText(buttonText2);

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
