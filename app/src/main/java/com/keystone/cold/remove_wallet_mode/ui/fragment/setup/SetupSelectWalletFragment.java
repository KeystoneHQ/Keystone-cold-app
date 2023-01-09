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

package com.keystone.cold.remove_wallet_mode.ui.fragment.setup;

import android.content.Intent;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSetupSelectWalletBinding;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;

public class SetupSelectWalletFragment extends SetupVaultBaseFragment<FragmentSetupSelectWalletBinding> {

    @Override
    protected int setView() {
        return R.layout.fragment_setup_select_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.tvConnectLater.setOnClickListener(v -> onLater());
    }

    private void onLater() {
        startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();
    }


}
