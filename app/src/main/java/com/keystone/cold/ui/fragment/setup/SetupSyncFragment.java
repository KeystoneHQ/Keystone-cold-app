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

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupSyncBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.viewmodel.SyncViewModel;

public class SetupSyncFragment extends SetupVaultBaseFragment<SetupSyncBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_sync;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(this::complete);
        generateSyncData();
    }

    private void complete(View view) {
        Intent intent = new Intent(mActivity, MainActivity.class);
        startActivity(intent);
        mActivity.finish();
    }

    private void generateSyncData() {
        ViewModelProviders.of(mActivity).get(SyncViewModel.class)
                .generateSyncKeystone().observe(this, sync -> {
            if (!TextUtils.isEmpty(sync)) {
                mBinding.sync.qrcodeLayout.qrcode.setData(sync);
            }
        });
    }
}
