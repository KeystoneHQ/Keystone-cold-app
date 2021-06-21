package com.keystone.cold.sdcard;

import android.os.Handler;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FormatSdcardHintModalBinding;
import com.keystone.cold.databinding.FormatSdcardProgressModalBinding;
import com.keystone.cold.databinding.ModalWithTwoButtonBinding;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.List;

import static android.content.Context.STORAGE_SERVICE;

public class SdcardFormatHelper {

    private final Handler handler = new Handler();
    private ModalDialog formatDialog;

    public void showFormatModal(AppCompatActivity activity) {
        Log.w("Storage", "showFormatSdcardModal ");
        if (formatDialog != null && formatDialog.getDialog() != null
                && formatDialog.getDialog().isShowing()) {
            return;
        }
        StorageManager storageManager = (StorageManager) activity.getSystemService(STORAGE_SERVICE);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        DiskInfo targetDisk = null;
        for (DiskInfo disk : storageManager.getDisks()) {
            if (volumes.stream().noneMatch(volumeInfo -> disk.equals(volumeInfo.disk))) {
                targetDisk = disk;
                break;
            }
        }
        if (targetDisk == null) {
            return;
        }

        formatDialog = ModalDialog.newInstance();
        FormatSdcardHintModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.format_sdcard_hint_modal,
                null, false);
        formatDialog.setBinding(binding);
        binding.close.setOnClickListener(v -> {
            formatDialog.dismiss();
            formatDialog = null;
        });
        DiskInfo finalTargetDisk = targetDisk;
        binding.confirm.setOnClickListener(v -> {
            formatDialog.dismiss();
            formatDialog = null;
            showFormatProgress(activity, storageManager, finalTargetDisk);
        });
        formatDialog.show(activity.getSupportFragmentManager(), "FormatModal");
    }

    public void showFormatProgress(AppCompatActivity activity, StorageManager storageManager, DiskInfo disk) {
        ModalDialog progress = ModalDialog.newInstance();
        FormatSdcardProgressModalBinding binding1 = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.format_sdcard_progress_modal, null, false);
        progress.setBinding(binding1);
        progress.show(activity.getSupportFragmentManager(), "");
        AppExecutors.getInstance().networkIO().execute(() -> {
            boolean success;
            try {
                storageManager.partitionPublic(disk.id);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(progress::dismiss);
                handler.post(() -> showFormatFailed(activity, storageManager, disk));
                success = false;
            }
            if (success) {
                handler.post(() -> {
                    binding1.progress.setVisibility(View.GONE);
                    binding1.success.setVisibility(View.VISIBLE);
                    binding1.text.setText(activity.getString(R.string.format_success));
                });
                handler.postDelayed(progress::dismiss, 500);
            }
        });
    }

    private void showFormatFailed(AppCompatActivity activity, StorageManager storageManager, DiskInfo diskInfo) {
        ModalDialog dialog = new ModalDialog();
        ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.modal_with_two_button, null, false);
        binding.title.setText(activity.getString(R.string.format_failed));
        binding.subTitle.setText(activity.getString(R.string.retry_format));
        binding.subTitle.setGravity(Gravity.START);
        binding.left.setText(R.string.cancel);
        binding.left.setOnClickListener(v -> dialog.dismiss());
        binding.right.setText(activity.getString(R.string.retry));
        binding.right.setOnClickListener(v -> {
            dialog.dismiss();
            showFormatProgress(activity, storageManager, diskInfo);
        });
        dialog.setBinding(binding);
        dialog.show(activity.getSupportFragmentManager(), "");
    }

    public boolean needFormatSdcard() {
        StorageManager storageManager = (StorageManager) MainApplication.getApplication().getSystemService(STORAGE_SERVICE);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        for (DiskInfo disk : storageManager.getDisks()) {
            return volumes.stream().noneMatch(volumeInfo -> disk.equals(volumeInfo.disk));
        }
        return false;
    }
}
