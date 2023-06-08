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

package com.keystone.cold.remove_wallet_mode.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.DialogCardanoAccountPickerBinding;
import com.keystone.cold.databinding.DialogPickerBinding;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;

import java.util.Objects;
import java.util.stream.IntStream;

public class CardanoAccountPicker extends DialogFragment {

    private NumberPickerCallback mCallback;
    private static final int MIN = 0;
    private static final int MAX = 23;

    public CardanoAccountPicker(){

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

        DialogCardanoAccountPickerBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cardano_account_picker,
                container, false);
        String[] displayValue = IntStream.range(MIN, MAX + 1)
                .mapToObj(String::valueOf)
                .toArray(String[]::new);
        binding.setValue(0);
        binding.picker.setValue(0);
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(MIN);
        binding.picker.setMaxValue(MAX);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal));
        binding.confirm.setOnClickListener(v -> {
            dismiss();
            if (mCallback != null) {
                mCallback.onValueSet(binding.picker.getValue());
            }
        });
        binding.cancel.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }
}
