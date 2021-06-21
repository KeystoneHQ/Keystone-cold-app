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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.DeleteWhiteListModalBinding;
import com.keystone.cold.databinding.ManageWhiteListBinding;
import com.keystone.cold.databinding.WhiteListItemBinding;
import com.keystone.cold.db.entity.WhiteListEntity;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.KeyStoreUtil;
import com.keystone.cold.viewmodel.WhiteListModel;

import java.util.ArrayList;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

public class ManageWhiteListFragment extends BaseFragment<ManageWhiteListBinding>
        implements Toolbar.OnMenuItemClickListener {


    private boolean isEdit = false;
    private WhiteListAdapter adapter;
    private WhiteListModel model;

    @Override
    protected int setView() {
        return R.layout.manage_white_list;
    }

    @Override
    protected void init(View view) {
        Bundle data = getArguments();
        if (data != null && data.getBoolean(IS_SETUP_VAULT)) {
            mBinding.toolbar.setNavigationOnClickListener(v ->
                    navigate(R.id.action_setupManageWhiteList_to_setupSyncFragment, data));
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        }
        mBinding.toolbar.inflateMenu(R.menu.white_list);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        adapter = new WhiteListAdapter(mActivity);
        mBinding.list.setAdapter(adapter);
        model = ViewModelProviders.of(this).get(WhiteListModel.class);
        subscribe(model);
    }

    private void subscribe(WhiteListModel model) {
        model.getWhiteList().observe(this, whiteListEntities -> {
            if (whiteListEntities != null && !whiteListEntities.isEmpty()) {
                adapter.setItems(whiteListEntities);
            } else {
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                refreshEditStatus(item);
                break;
            case R.id.action_add:
                if (isEdit) {
                    refreshEditStatus(mBinding.toolbar.getMenu().findItem(R.id.action_edit));
                }
                navigate(R.id.add_white_list);
                break;
            default:
                break;
        }
        return true;
    }

    private void refreshEditStatus(MenuItem menuItem) {
        isEdit = !isEdit;
        adapter.notifyDataSetChanged();
        menuItem.setIcon(isEdit ? R.drawable.checked : R.drawable.edit);
    }

    class WhiteListAdapter extends BaseBindingAdapter<WhiteListEntity, WhiteListItemBinding> {

        WhiteListAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.white_list_item;
        }

        @Override
        protected void onBindItem(WhiteListItemBinding binding, WhiteListEntity item) {
            String encryptedAddr = item.getAddr();
            String decryptedAddr = new String(new KeyStoreUtil().decrypt(ByteFormatter.hex2bytes(encryptedAddr)));

            binding.setIsEdit(isEdit);
            binding.setItem(item);
            binding.addr.setText(decryptedAddr);
            binding.delete.setOnClickListener(v -> delete(item));
        }
    }

    private void delete(WhiteListEntity item) {
        ModalDialog dialog = ModalDialog.newInstance();
        DeleteWhiteListModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.delete_white_list_modal, null, false);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setOnClickListener(v -> {
            doDelete(item);
            dialog.dismiss();
        });
        String addr = new String(new KeyStoreUtil().decrypt(ByteFormatter.hex2bytes(item.getAddr())));
        binding.subTitle.setText(getString(R.string.delete_white_list_hint,
                item.getAddrName() + "-" + addr));
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void doDelete(WhiteListEntity item) {
        model.deleteWhiteList(item);
    }
}
