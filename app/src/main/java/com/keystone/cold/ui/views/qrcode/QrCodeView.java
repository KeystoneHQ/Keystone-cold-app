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

package com.keystone.cold.ui.views.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.QrcodeModalBinding;
import com.keystone.cold.ui.modal.FullScreenModal;

public class QrCodeView extends LinearLayout implements QrCodeHolder, QrCode {

    private String data;
    private final Cache mCache = Cache.getInstance();
    private ProgressBar progressBar;
    private ImageView img;

    public QrCodeView(Context context) {
        this(context, null);
    }

    public QrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public void setData(String s) {
        data = s;
        showQrCode();
    }

    public void disableModal() {
        img.setOnClickListener(null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        img = findViewById(R.id.img);
        img.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(data)) {
                showModal();
            }
        });
        progressBar = findViewById(R.id.progress);
    }

    private void showQrCode() {
        if (ViewCompat.isLaidOut(this)) {
            mCache.offer(data, QrCodeView.this);
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mCache.offer(data, QrCodeView.this);
                }
            });
        }
    }

    public void showModal() {
        FullScreenModal dialog = new FullScreenModal();
        QrcodeModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.qrcode_modal, null, false);
        dialog.setBinding(binding);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.qrcodeLayout.qrcode.setData(data);
        binding.qrcodeLayout.qrcode.disableModal();
        setupSeekbar(binding);
        dialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "");
    }

    private void setupSeekbar(QrcodeModalBinding binding) {
        float min = 0.5f;
        float max = 1.15f;
        float step = (max - min) / 100;
        binding.seekbar.setProgress((int) ((1 - min) / step));
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float scale = 0.5f + step * i;
                binding.qrcodeLayout.qrcode.setScaleX(scale);
                binding.qrcodeLayout.qrcode.setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setImageBitmap(Bitmap bm) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            progressBar.setVisibility(GONE);
            img.setVisibility(VISIBLE);
            img.setImageBitmap(bm);
        });
    }

    @Override
    public void onRender(Bitmap bm) {
        setImageBitmap(bm);
    }

    @Override
    public int getViewWidth() {
        return img.getWidth();
    }

    @Override
    public int getViewHeight() {
        return img.getHeight();
    }
}
