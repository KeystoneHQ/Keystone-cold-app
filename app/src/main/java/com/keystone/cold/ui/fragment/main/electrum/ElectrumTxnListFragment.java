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

package com.keystone.cold.ui.fragment.main.electrum;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ElectrumTxnBinding;
import com.keystone.cold.databinding.TxnListBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.ElectrumViewModel;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.keystone.cold.update.utils.Storage.hasSdcard;

public class ElectrumTxnListFragment extends BaseFragment<TxnListBinding>
        implements Callback {

    public static final String TAG = "ElectrumTxnListFragment";
    private ElectrumViewModel viewModel;
    private TxnAdapter adapter;
    private AtomicBoolean showEmpty;

    @Override
    protected int setView() {
        return R.layout.txn_list;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        mBinding.toolbar.setTitle("");
        viewModel = ViewModelProviders.of(mActivity).get(ElectrumViewModel.class);
        adapter = new TxnAdapter(mActivity, this);
        initViews();
    }

    private void initViews() {
        showEmpty = new AtomicBoolean(false);
        if (!hasSdcard()) {
            showEmpty.set(true);
            mBinding.emptyTitle.setText(R.string.no_sdcard);
            mBinding.emptyMessage.setText(R.string.no_sdcard_hint);
        } else {
            mBinding.list.setAdapter(adapter);
            viewModel.loadUnsignTxn().observe(this, files -> {
                if (files.size() > 0) {
                    adapter.setItems(files);
                } else {
                    showEmpty.set(true);
                    mBinding.emptyTitle.setText(R.string.no_unsigned_txn);
                    mBinding.emptyMessage.setText(R.string.no_unsigned_txn_hint);
                }
                updateUi();
            });
        }
        updateUi();
    }

    private void updateUi() {
        if (showEmpty.get()) {
            mBinding.emptyView.setVisibility(View.VISIBLE);
            mBinding.list.setVisibility(View.GONE);
        } else {
            mBinding.emptyView.setVisibility(View.GONE);
            mBinding.list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onClick(String file) {
        viewModel.parseTxnFile(file).observe(this, hex -> {
            if (!TextUtils.isEmpty(hex)) {
                Bundle bundle = new Bundle();
                bundle.putString("txn", hex);
                bundle.putBoolean("is_file", true);
                navigate(R.id.action_to_ElectrumTxConfirmFragment, bundle);
            } else {
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.electrum_decode_txn_fail),
                        getString(R.string.error_txn_file),
                        getString(R.string.confirm),
                        null);
            }
        });
    }


    public static class TxnAdapter extends BaseBindingAdapter<String, ElectrumTxnBinding> {
        private Callback callback;

        TxnAdapter(Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.electrum_txn;
        }

        @Override
        protected void onBindItem(ElectrumTxnBinding binding, String item) {
            binding.setFile(item);
            binding.setCallback(callback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
        }
    }


}
