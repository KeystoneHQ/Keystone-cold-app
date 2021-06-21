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

package com.keystone.cold.ui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.PickerDialogBinding;

import java.util.Objects;
import java.util.stream.IntStream;

public class AddressNumberPicker extends DialogFragment {

    private NumberPickerCallback mCallback;
    private static final int MIN = 0;
    private static final int MAX = 8;

    public AddressNumberPicker(){

    }

    public void setCallback(NumberPickerCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).requestWindowFeature(STYLE_NO_TITLE);

        PickerDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.picker_dialog,
                container, false);
        String[] displayValue = IntStream.range(MIN, MAX + 1)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        binding.setValue(1);
        binding.picker.setValue(0);
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(MIN);
        binding.picker.setMaxValue(MAX);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal + 1));
        binding.confirm.setOnClickListener(v -> {
            dismiss();
            if (mCallback != null) {
                mCallback.onValueSet(binding.picker.getValue() + 1);
            }
        });
        binding.cancel.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }
}
