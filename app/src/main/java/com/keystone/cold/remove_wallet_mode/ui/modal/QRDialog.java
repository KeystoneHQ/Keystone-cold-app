package com.keystone.cold.remove_wallet_mode.ui.modal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.DialogQrBinding;

public class QRDialog extends DialogFragment {

    public static QRDialog show(AppCompatActivity activity, String qrData, String subTittle) {
        QRDialog qrDialog = new QRDialog();
        qrDialog.data = qrData;
        qrDialog.subTittle = subTittle;
        qrDialog.show(activity.getSupportFragmentManager(), "");
        return qrDialog;
    }

    private String subTittle;
    private String data;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogQrBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_qr, null, false);
        binding.ivClose.setOnClickListener(v -> dismiss());
        binding.qrcode.setData(data);
        binding.qrcode.disableModal();
        binding.tvSubTittle.setText(subTittle);
        binding.tvLink.setText(data);
        Dialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialog)
                .setView(binding.getRoot())
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
