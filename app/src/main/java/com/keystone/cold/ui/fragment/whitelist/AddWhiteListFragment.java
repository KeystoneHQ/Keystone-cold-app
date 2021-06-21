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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AddWhiteListBinding;
import com.keystone.cold.databinding.CommonPickerDialogBinding;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.WhiteListEntity;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.KeyStoreUtil;
import com.keystone.cold.util.Keyboard;
import com.keystone.cold.viewmodel.SharedDataViewModel;
import com.keystone.cold.viewmodel.WhiteListModel;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class AddWhiteListFragment extends BaseFragment<AddWhiteListBinding>
        implements Toolbar.OnMenuItemClickListener {

    private WhiteListModel model;

    private final ObservableField<String> name = new ObservableField<>();
    private final ObservableField<String> addr = new ObservableField<>();
    private final ObservableField<String> memo = new ObservableField<>();

    @Override
    protected int setView() {
        return R.layout.add_white_list;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.inflateMenu(R.menu.add_white_list);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        model = ViewModelProviders.of(this).get(WhiteListModel.class);

        mBinding.add.setOnClickListener(this::add);
        mBinding.coinCode.setOnClickListener(this::showPicker);
        mBinding.setName(name);
        mBinding.setAddress(addr);
        mBinding.setMemo(memo);
    }

    private void showPicker(View view) {
        model.getSupportCoins().observe(this, coinEntities -> {

            final String[] coins = coinEntities.stream()
                    .map(CoinEntity::getCoinCode)
                    .toArray(String[]::new);

            ModalDialog dialog = ModalDialog.newInstance();
            CommonPickerDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.common_picker_dialog, null, false);
            binding.picker.setDisplayedValues(coins);
            binding.picker.setMaxValue(coinEntities.size() - 1);
            binding.picker.setMinValue(0);
            binding.btnOk.setOnClickListener(v -> {
                String coinCode = coins[binding.picker.getValue()];
                mBinding.coinCode.setText(coinCode);
                mBinding.memoEdit.setVisibility(shouldShowMemo(coinCode) ? View.VISIBLE : View.GONE);
                dialog.dismiss();
            });

            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        });

    }

    private boolean shouldShowMemo(String coinCode) {
        return "EOS".equals(coinCode) || "XRP".equals(coinCode);
    }

    private void add(View view) {
        byte[] encryptedBytes = new KeyStoreUtil().encrypt(Objects.requireNonNull(addr.get())
                .getBytes(StandardCharsets.UTF_8));
        String encryptedAddr = ByteFormatter.bytes2hex(encryptedBytes);
        WhiteListEntity entity = new WhiteListEntity(
                encryptedAddr,
                name.get(),
                mBinding.coinCode.getText().toString(),
                memo.get()
        );
        entity.setBelongTo(Utilities.getCurrentBelongTo(mActivity));
        model.insertWhiteList(entity);
        navigateUp();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            SharedDataViewModel viewModel =
                    ViewModelProviders.of(mActivity).get(SharedDataViewModel.class);
            viewModel.getScanResult().observe(this, addr::set);
            Keyboard.hide(mActivity, Objects.requireNonNull(getView()));
            Bundle data = new Bundle();
            data.putString("purpose", "address");
            navigate(R.id.add_white_list_scan, data);
        }
        return true;
    }
}
