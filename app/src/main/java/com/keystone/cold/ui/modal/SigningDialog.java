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

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.LauoutSigningBinding;


public class SigningDialog extends DialogFragment {

    public static final int STATE_SIGNING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAIL = 2;

    private LauoutSigningBinding mBinding;

    @IntDef({STATE_SIGNING, STATE_SUCCESS, STATE_FAIL})
    public @interface State {
    }

    public static SigningDialog newInstance() {
        return new SigningDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.lauout_signing, null, false);
        Dialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialog)
                .setView(mBinding.getRoot())
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public void setState(@State int state) {
        mBinding.setState(state);
    }

    public void updateProgress(@IntRange(from = 0, to = 100) int progress) {
        mBinding.setProgress(progress);
    }
}
