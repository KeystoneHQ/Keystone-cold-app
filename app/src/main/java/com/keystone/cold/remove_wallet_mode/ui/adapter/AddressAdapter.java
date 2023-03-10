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

package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemAddressListBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;
import com.keystone.cold.util.Keyboard;

import java.util.Objects;

public class AddressAdapter extends FilterableBaseBindingAdapter<AddressItem, ItemAddressListBinding> {

    private View focusView;
    private View activeIcon;
    private boolean isEditing;
    private final AddressCallback mAddressCallback;

    public AddressAdapter(Context context, AddressCallback callback) {
        super(context);
        mAddressCallback = callback;

    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_address_list;
    }

    @Override
    protected void onBindItem(ItemAddressListBinding binding, AddressItem item) {
        binding.setAddressItem(item);
        binding.setIsEditing(false);
        binding.editIcon.setOnClickListener(v -> {

            if (!isEditing) {
                isEditing = true;
                if (activeIcon != null) {
                    activeIcon.setAlpha(0.5f);
                }
                activeIcon = v;
                v.setAlpha(1f);
                binding.name.setEnabled(true);
                binding.name.requestFocus();
                binding.name.setSelection(binding.name.getText().length());
                Keyboard.show(binding.name.getContext(), binding.name);
            } else if (v == activeIcon) {
                exitEdit();
            }
        });

        binding.name.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(binding.name.getText()).toString() != null) {
                    if (!binding.getAddressItem().getName().equals(binding.name.getText().toString()))
                        binding.getAddressItem().setName(binding.name.getText().toString());
                }
                mAddressCallback.onNameChange(binding.getAddressItem());
            } else {
                if (isEditing) {
                    view.setAlpha(1);
                    focusView = view;
                }
            }
        });

        binding.name.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Keyboard.hide(binding.name.getContext(), binding.name);
                binding.name.clearFocus();
                binding.name.setEnabled(false);
                isEditing = false;
                binding.editIcon.setAlpha(0.5f);
                activeIcon = null;
            }
            return false;
        });


        binding.name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        binding.setCallback(mAddressCallback);
    }

    public void exitEdit() {
        if (focusView != null) {
            focusView.clearFocus();
            Keyboard.hide(focusView.getContext(), focusView);
            if (activeIcon != null) {
                activeIcon.setAlpha(0.5f);
            }
            focusView.setEnabled(false);
            isEditing = false;
            activeIcon = null;
        }
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void enterSearch() {
        if (focusView != null) {
            focusView.clearFocus();
            if (activeIcon != null) {
                activeIcon.setAlpha(0.5f);
            }
            isEditing = false;
        }
    }



    public interface AddressCallback {
        void onClick(AddressItem addr);

        void onNameChange(AddressItem addr);
    }
}
