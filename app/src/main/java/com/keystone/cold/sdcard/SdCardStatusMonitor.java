package com.keystone.cold.sdcard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.keystone.cold.MainApplication;

import java.util.HashMap;
import java.util.Map;

public class SdCardStatusMonitor extends BroadcastReceiver {

    private static SdCardStatusMonitor instance;
    private final Map<String, OnSdcardStatusChange> callbacks = new HashMap<>();

    public static SdCardStatusMonitor getInstance(MainApplication context) {
        if (instance == null) {
            synchronized (SdCardStatusMonitor.class) {
                if (instance == null) {
                    instance = new SdCardStatusMonitor(context);
                }
            }
        }
        return instance;
    }

    private SdCardStatusMonitor(MainApplication context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        context.registerReceiver(this, intentFilter);
    }

    public void register(OnSdcardStatusChange callback) {
        if (!callbacks.containsKey(callback.id())) {
            callbacks.put(callback.id(), callback);
        }
    }

    public void unregister(OnSdcardStatusChange callback) {
        callbacks.remove(callback.id());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("SdCardStatusMonitor", "action: " + intent.getAction());
        if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
            for (OnSdcardStatusChange callback : callbacks.values()) {
                callback.onInsert();
            }
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())
                || Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction())
                || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(intent.getAction())) {
            for (OnSdcardStatusChange callback : callbacks.values()) {
                callback.onRemove();
            }
        }
    }

}
