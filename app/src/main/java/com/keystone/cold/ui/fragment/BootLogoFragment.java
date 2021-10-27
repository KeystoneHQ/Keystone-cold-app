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

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.BootLogoBinding;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

public class BootLogoFragment extends BaseFragment<BootLogoBinding> {

    private static final int SPLASH_DURATION = 0;

    @Override
    protected int setView() {
        return R.layout.boot_logo;
    }

    @Override
    protected void init(View view) {
        new Handler().postDelayed(() -> {
            Log.d("sora", "init: hasPasswordSet " + Utilities.hasPasswordSet(mActivity));
            Log.d("sora", "init: hasVaultCreated " + Utilities.hasVaultCreated(mActivity));
            Log.d("sora", "init: hasLanguageSet " + Utilities.hasLanguageSet(mActivity));
            Log.d("sora", "init: getVaultCreateStep " + Utilities.getVaultCreateStep(mActivity.getApplication()));
            if (Utilities.hasLanguageSet(mActivity) && Utilities.getVaultCreateStep(mActivity.getApplication()) != SetupVaultViewModel.VAULT_CREATE_STEP_WELCOME) {
                navigate(R.id.action_splash_to_securityCheck);
            } else {
                navigate(R.id.action_splash_to_setupLanguage);
            }
        }, SPLASH_DURATION);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
