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

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupFirmwareUpgradeBinding;

public class FirmwareUpgradeFragment extends SetupVaultBaseFragment<SetupFirmwareUpgradeBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_firmware_upgrade;
    }

    @Override
    protected void init(View view) {
        super.init(view);

        mBinding.confirm.setOnClickListener((v) -> {
            Bundle data = new Bundle();
            data.putBoolean(IS_SETUP_VAULT, true);
            navigate(R.id.action_to_setupVaultFragment, data);
        });
        mBinding.toolbar.setVisibility(View.GONE);

    }
}
