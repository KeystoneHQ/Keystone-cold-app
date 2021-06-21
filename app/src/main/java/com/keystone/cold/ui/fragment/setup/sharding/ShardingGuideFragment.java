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
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ShardingGuideBinding;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;

import java.util.Objects;

public class ShardingGuideFragment extends SetupVaultBaseFragment<ShardingGuideBinding> {

    private int total = 5;
    private int threshold = 3;

    @Override
    protected int setView() {
        return R.layout.sharding_guide;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = requireArguments();
        total = data.getInt("total");
        threshold = data.getInt("threshold");
        mBinding.guide.setText(getString(R.string.shading_guide, threshold, total, total, threshold));
        mBinding.confirm.setOnClickListener(v-> {
            viewModel.generateSlip39Mnemonic(threshold,total);
            navigate(R.id.action_to_preCreateShardingFragment);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
