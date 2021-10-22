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

package com.keystone.cold.ui.modal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;


public class ModalDialog extends DialogFragment {

    private ViewDataBinding binding;

    private Runnable runOnResume;

    public static ModalDialog newInstance() {
        return new ModalDialog();
    }

    public void setBinding(ViewDataBinding binding) {
        this.binding = binding;
    }

    public void setRunOnResume(Runnable runOnResume) {
        this.runOnResume = runOnResume;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialog)
                .setView(binding.getRoot())
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static ModalDialog showCommonModal(AppCompatActivity activity,
                                              String title,
                                              String subTitle,
                                              String buttonText,
                                              Runnable confirmAction) {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.common_modal, null, false);
        binding.title.setText(title);
        binding.subTitle.setText(subTitle);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(buttonText);
        binding.confirm.setOnClickListener(v -> {
            if (confirmAction != null) {
                confirmAction.run();
            }
            dialog.dismiss();
        });
        dialog.setBinding(binding);
        dialog.show(activity.getSupportFragmentManager(), "");
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.runOnResume != null) {
            runOnResume.run();
        }
    }
}
