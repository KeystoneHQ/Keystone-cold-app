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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ModalWithTwoButtonBinding;
import com.keystone.cold.databinding.PreCreateShardingBinding;
import com.keystone.cold.databinding.SecretModalBinding;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

public class PreCreateShardingFragment extends SetupVaultBaseFragment<PreCreateShardingBinding>
        implements Toolbar.OnMenuItemClickListener{

    @Override
    protected int setView() {
        return R.layout.pre_create_sharding;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.inflateMenu(R.menu.cancel);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.sequence.setText(getString(R.string.generate_sharding_sequence,
                viewModel.currentSequence() + 1, viewModel.totalShares()));
        mBinding.hint.setText(getString(R.string.generate_shading_hint,viewModel.currentSequence() + 1));
        mBinding.confirm.setOnClickListener(v -> {
            ModalDialog dialog = new ModalDialog();
            SecretModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.secret_modal, null, false);
            dialog.setBinding(binding);
            binding.know.setOnClickListener(button -> {
                navigate(R.id.action_to_generateMnemonicFragment);
                dialog.dismiss();
            });
            dialog.show(mActivity.getSupportFragmentManager(),"");
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cancel) {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button,
                    null,false);
            binding.title.setText(R.string.confirm_cancel_create_sharding_title);
            binding.subTitle.setText(R.string.confirm_cancel_create_sharding);
            binding.left.setText(getString(R.string.confirm_cancel_import_sharding));
            binding.left.setOnClickListener(v -> {
                dialog.dismiss();
                popBackStack(R.id.tabletQrcodeFragment,false);
            });
            binding.right.setText(R.string.continue_create_sharding);
            binding.right.setOnClickListener(v -> dialog.dismiss());
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(),"");
        }
        return true;
    }
}
