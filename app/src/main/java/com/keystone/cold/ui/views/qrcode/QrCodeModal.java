/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.ui.views.qrcode;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.cold.R;
import com.keystone.cold.databinding.DynamicQrcodeModalBinding;
import com.keystone.cold.databinding.SwitchQrCapacityBottomSheetBinding;

import java.util.Objects;

public class QrCodeModal extends DialogFragment {
    private DynamicQrcodeModalBinding modalBinding;
    private String data;

    public static QrCodeModal newInstance(String data, boolean multipart) {
        Bundle args = new Bundle();
        args.putString("data", data);
        args.putBoolean("multipart", multipart);
        QrCodeModal fragment = new QrCodeModal();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = this.getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        modalBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dynamic_qrcode_modal, null, false);
        modalBinding.close.setOnClickListener(v -> dismiss());
        Bundle bundle = requireArguments();
        data = bundle.getString("data");
        boolean multipart = bundle.getBoolean("multipart");

        if (!multipart) {
            modalBinding.qrcodeLayout.qrcode.disableMultipart();
        }
        modalBinding.qrcodeLayout.qrcode.disableModal();
        setupSeekbar();
        setupController();
        setupCapacitySwitch();
        modalBinding.qrcodeLayout.qrcode.getURSubscriber().observe(getActivity(), ur -> {
            if (!multipart || ur.getCborBytes().length <= DynamicQrCodeView.QrCapacity.LOW.capacity) {
                modalBinding.switchCapacity.setVisibility(View.GONE);
            }
        });
        updateUI();
        return modalBinding.getRoot();
    }

    private int getCapacityString(DynamicQrCodeView.QrCapacity qrCapacity) {
        switch (qrCapacity) {
            case MID:
                return R.string.qr_capacity_mid;
            case LOW:
                return R.string.qr_capacity_low;
            default:
                return R.string.qr_capacity_high;
        }
    }

    private void setupController() {
        if (modalBinding == null) return;
        DynamicQrCodeView qr = modalBinding.qrcodeLayout.qrcode;
        if (modalBinding.qrcodeLayout.qrcode.count < 2) {
            modalBinding.animateController.setVisibility(View.INVISIBLE);
        } else {
            modalBinding.animateController.setVisibility(View.VISIBLE);
            modalBinding.pause.setOnClickListener(v -> {
                if (qr.autoAnimate) {
                    qr.setAutoAnimate(false);
                    modalBinding.pause.setImageResource(R.drawable.resume);
                    modalBinding.prev.setEnabled(true);
                    modalBinding.next.setEnabled(true);
                    modalBinding.prev.setOnClickListener(prev -> {
                        qr.currentIndex = (qr.currentIndex - 1 + qr.count) % qr.count;
                        qr.showQrCode();
                    });

                    modalBinding.next.setOnClickListener(prev -> {
                        qr.currentIndex = (qr.currentIndex + 1) % qr.count;
                        qr.showQrCode();
                    });
                } else {
                    qr.setAutoAnimate(true);
                    modalBinding.pause.setImageResource(R.drawable.pause);
                    modalBinding.prev.setEnabled(false);
                    modalBinding.next.setEnabled(false);
                }
            });
        }
    }

    private void setupSeekbar() {
        if (modalBinding == null) return;
        float min = 0.5f;
        float max = 1.15f;
        float step = (max - min) / 100;
        modalBinding.seekbar.setProgress((int) ((1 - min) / step));
        modalBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float scale = 0.5f + step * i;
                modalBinding.qrcodeLayout.qrcode.setScaleX(scale);
                modalBinding.qrcodeLayout.qrcode.setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setupCapacitySwitch() {
        if (modalBinding == null) return;
        modalBinding.switchCapacity.setOnClickListener(v -> showBottomMenu());
    }

    private void showBottomMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
        SwitchQrCapacityBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.switch_qr_capacity_bottom_sheet, null, false);
        refreshCheckedStatus(binding.getRoot());
        binding.qrHigh.setOnClickListener(v -> onCapacitySwitch(dialog, DynamicQrCodeView.QrCapacity.HIGH));
        binding.qrMid.setOnClickListener(v -> onCapacitySwitch(dialog, DynamicQrCodeView.QrCapacity.MID));
        binding.qrLow.setOnClickListener(v -> onCapacitySwitch(dialog, DynamicQrCodeView.QrCapacity.LOW));
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void refreshCheckedStatus(View view) {
        for (DynamicQrCodeView.QrCapacity capacity : DynamicQrCodeView.QrCapacity.values()) {
            view.findViewWithTag(capacity.toString()).setVisibility(View.GONE);
        }
        view.findViewWithTag(modalBinding.qrcodeLayout.qrcode.qrCapacity.toString()).setVisibility(View.VISIBLE);
    }

    private void onCapacitySwitch(BottomSheetDialog dialog,
                                  DynamicQrCodeView.QrCapacity capacity) {
        modalBinding.qrcodeLayout.qrcode.qrCapacity = capacity;
        dialog.dismiss();
        updateUI();
    }

    private void updateUI() {
        modalBinding.qrcodeLayout.qrcode.currentIndex = 0;
        modalBinding.qrcodeLayout.qrcode.handler.removeCallbacks(modalBinding.qrcodeLayout.qrcode.runnable);
        modalBinding.qrcodeLayout.qrcode.setData(data);
        int capacityString = getCapacityString(modalBinding.qrcodeLayout.qrcode.qrCapacity);
        modalBinding.switchCapacity.setText(getString(R.string.qrcode_capacity, getString(capacityString)));
    }
}
