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
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.DialogBitkeepPathsBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.BitKeepPathItem;

import java.util.List;

public class BitKeepPathsDialog extends DialogFragment {

    public interface BitKeepOnSelectCallBack {
        void onClick(String code);
    }

    public BitKeepPathsDialog(List<BitKeepPathItem> bitKeepPathItems, BitKeepOnSelectCallBack onComplete, String code) {
        this.bitKeepPathItems = bitKeepPathItems;
        this.onComplete = onComplete;
        this.code = code;
    }

    public static BitKeepPathsDialog show(AppCompatActivity activity, List<BitKeepPathItem> bitKeepPathItems, BitKeepOnSelectCallBack callBack) {
        BitKeepPathsDialog qrDialog = new BitKeepPathsDialog(bitKeepPathItems, callBack, Utilities.getCurrentBTCAccount(activity));
        qrDialog.show(activity.getSupportFragmentManager(), "");
        return qrDialog;
    }

    private List<BitKeepPathItem> bitKeepPathItems;
    private BitKeepOnSelectCallBack onComplete;
    private String code;

    public void uncheckAll() {
        if (bitKeepPathItems != null) {
            for (int i = 0; i < bitKeepPathItems.size(); i++) {
                BitKeepPathItem item = bitKeepPathItems.get(i);
                item.getIsSelect().set(false);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogBitkeepPathsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_bitkeep_paths, null, false);
        binding.ivClose.setOnClickListener(v -> dismiss());
        Dialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialog)
                .setView(binding.getRoot())
                .create();
        BitKeepPathItem item1 = bitKeepPathItems.get(0);
        BitKeepPathItem item2 = bitKeepPathItems.get(1);
        BitKeepPathItem item3 = bitKeepPathItems.get(2);
        binding.setPathItem1(item1);
        binding.setPathItem2(item2);
        binding.setPathItem3(item3);
        binding.item1.setOnClickListener((v) -> {
            code = item1.getCode();
            item1.getIsSelect().set(true);
            item2.getIsSelect().set(false);
            item3.getIsSelect().set(false);
        });
        binding.item2.setOnClickListener((v) -> {
            code = item2.getCode();
            item1.getIsSelect().set(false);
            item2.getIsSelect().set(true);
            item3.getIsSelect().set(false);
        });
        binding.item3.setOnClickListener((v) -> {
            code = item3.getCode();
            item1.getIsSelect().set(false);
            item2.getIsSelect().set(false);
            item3.getIsSelect().set(true);
        });
        binding.complete.setOnClickListener((v) -> {
            dialog.dismiss();
            onComplete.onClick(code);
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
