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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.ElectrumExportBinding;
import com.keystone.cold.databinding.ExportSdcardModalBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.update.utils.Storage;
import com.keystone.cold.viewmodel.ElectrumViewModel;

import static com.keystone.cold.viewmodel.ElectrumViewModel.exportSuccess;
import static com.keystone.cold.viewmodel.ElectrumViewModel.showNoSdcardModal;
import static com.keystone.cold.viewmodel.ElectrumViewModel.writeToSdcard;

public class ElectrumExportFragment extends BaseFragment<ElectrumExportBinding> {

    private static final String EXTEND_PUB_FILE_NAME = "p2wpkh-p2sh-pubkey.txt";
    private String exPub;

    @Override
    protected int setView() {
        return R.layout.electrum_export;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        ElectrumViewModel viewModel = ViewModelProviders.of(mActivity).get(ElectrumViewModel.class);
        viewModel.getMasterPublicKey().observe(this, s -> {
            if (!TextUtils.isEmpty(s)) {
                exPub = s;
                mBinding.qrcode.setData(s);
                mBinding.expub.setText(s);
            }
        });
        mBinding.info.setOnClickListener(v -> showElectrumInfo());
        mBinding.done.setOnClickListener(v->{
            MainActivity activity = (MainActivity) mActivity;
            activity.getNavController().popBackStack(R.id.assetFragment,false);
        });
        mBinding.exportToSdcard.setOnClickListener(v -> {
            Storage storage = Storage.createByEnvironment();
            if (storage == null || storage.getExternalDir() == null) {
                showNoSdcardModal(mActivity);
            } else {
                ModalDialog modalDialog = ModalDialog.newInstance();
                ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.export_sdcard_modal, null, false);
                binding.title.setText(R.string.export_xpub_text_file);
                binding.fileName.setText(EXTEND_PUB_FILE_NAME);
                binding.actionHint.setText(R.string.electrum_import_xpub_action);
                binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
                binding.confirm.setOnClickListener(vv -> {
                    modalDialog.dismiss();
                    if (writeToSdcard(storage, exPub, EXTEND_PUB_FILE_NAME)) {
                        exportSuccess(mActivity, null);
                    }
                });
                modalDialog.setBinding(binding);
                modalDialog.show(mActivity.getSupportFragmentManager(), "");
            }
        });
    }

    private void showElectrumInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.electrum_import_xpub_guide_title);
        binding.subTitle.setText(R.string.electrum_import_xpub_action_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
