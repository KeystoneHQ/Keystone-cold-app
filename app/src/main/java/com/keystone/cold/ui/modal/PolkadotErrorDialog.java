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
import com.keystone.cold.databinding.PolkadotErrorModalBinding;
import com.keystone.cold.databinding.TwoButtonModalBinding;

import org.json.JSONArray;
import org.json.JSONException;


public class PolkadotErrorDialog extends DialogFragment {

    private ViewDataBinding binding;

    private Runnable runOnResume;

    public static PolkadotErrorDialog newInstance() {
        return new PolkadotErrorDialog();
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

    public static PolkadotErrorDialog show(AppCompatActivity activity,
                                                      String title,
                                                      String buttonText,
                                                      JSONArray content,
                                                      Runnable confirmAction) {
        PolkadotErrorDialog dialog = new PolkadotErrorDialog();
        PolkadotErrorModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.polkadot_error_modal, null, false);
        binding.title.setText(title);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(buttonText);
        binding.confirm.setOnClickListener(v -> {
            if (confirmAction != null) {
                confirmAction.run();
            }
            dialog.dismiss();
        });

        binding.dotTx.contentContainer.setBackgroundResource(R.drawable.modal_bg);
        binding.dotTx.container.setBackgroundResource(R.drawable.modal_bg);
        binding.dotTx.title.setVisibility(View.GONE);
        binding.dotTx.hint.setVisibility(View.GONE);
        binding.dotTx.icon.setVisibility(View.GONE);
        binding.dotTx.network.setVisibility(View.GONE);
        try {
            binding.dotTx.txDetail.updateUI(content, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
