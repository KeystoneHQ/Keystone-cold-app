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

package com.keystone.cold;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.keystone.coinlib.Coinlib;
import com.keystone.cold.callables.RestartSeCallable;
import com.keystone.cold.db.AppDatabase;
import com.keystone.cold.encryption.EncryptionCoreProvider;
import com.keystone.cold.logging.FileLogger;
import com.keystone.cold.sdcard.OnSdcardStatusChange;
import com.keystone.cold.sdcard.SdCardStatusMonitor;
import com.keystone.cold.sdcard.SdcardFormatHelper;
import com.keystone.cold.service.AttackCheckingService;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.UnlockActivity;

import java.lang.ref.SoftReference;

import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;
import static com.keystone.cold.viewmodel.WatchWallet.KEYSTONE;
import static com.keystone.cold.viewmodel.WatchWallet.METAMASK;
import static com.keystone.cold.viewmodel.WatchWallet.POLKADOT_JS;
import static com.keystone.cold.viewmodel.WatchWallet.XRP_TOOLKIT;

public class MainApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static MainApplication sApplication;
    private AppExecutors mAppExecutors;
    private boolean shouldLock;
    private boolean showFormatSdcard = false;

    public MainApplication() {
        sApplication = this;
    }

    @NonNull
    public static MainApplication getApplication() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        compatibleWalletSp();
        mAppExecutors = AppExecutors.getInstance();
        EncryptionCoreProvider.getInstance().initialize(this);
        mAppExecutors.diskIO().execute(() -> {
            FileLogger.init(this);
            FileLogger.purgeLogs(this);
        });
        initBackgroundCallBack();
        Coinlib.init(this);

        registerReceiver(mScreeOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        shouldLock = Utilities.hasVaultCreated(this);

        startAttackCheckingService();
        RestartSe();
        registerSdcardStatusMonitor();
    }

    private void compatibleWalletSp() {
        SharedPreferences prefs = Utilities.getPrefs(sApplication);
        String walletName = prefs.getString(SETTING_CHOOSE_WATCH_WALLET, KEYSTONE.getWalletId());
        switch (walletName) {
            case "0":
                prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, KEYSTONE.getWalletId()).apply();
                break;
            case "1":
                prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, POLKADOT_JS.getWalletId()).apply();
                break;
            case "2":
                prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, XRP_TOOLKIT.getWalletId()).apply();
                break;
            case "3":
                prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, METAMASK.getWalletId()).apply();
                break;
            default:
                break;
        }
    }

    private void registerSdcardStatusMonitor() {
        sdcardFormatHelper = new SdcardFormatHelper();
        SdCardStatusMonitor.getInstance(this).register(new OnSdcardStatusChange() {
            @Override
            public String id() {
                return "application";
            }

            @Override
            public void onInsert() {
                boolean needFormatSdcard = sdcardFormatHelper.needFormatSdcard();
                if (needFormatSdcard) {
                    if (topActivity.get() instanceof MainActivity) {
                        sdcardFormatHelper.showFormatModal(topActivity.get());
                        showFormatSdcard = false;
                    } else {
                        showFormatSdcard = true;
                    }
                } else {
                    showFormatSdcard = false;
                }
            }

            @Override
            public void onRemove() {
                showFormatSdcard = false;
            }
        });
        showFormatSdcard = sdcardFormatHelper.needFormatSdcard();
    }

    private void RestartSe() {
        if (Utilities.hasVaultCreated(this)) {
            mAppExecutors.diskIO().execute(() -> {
                boolean success = new RestartSeCallable().call();
                if (success) {
                    getRepository().deleteHiddenVaultData();
                    Utilities.setCurrentBelongTo(this, "main");
                }
            });
        }
    }

    private void startAttackCheckingService() {
        Intent intent = new Intent(this, AttackCheckingService.class);
        startService(intent);
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(this, getDatabase());
    }

    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (activity instanceof MainActivity && savedInstanceState == null && shouldLock) {
                    Intent intent = new Intent(activity, UnlockActivity.class);
                    activity.startActivity(intent);
                    shouldLock = false;
                }
                if (activity instanceof MainActivity && savedInstanceState != null) {
                    showFormatSdcard = false;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                topActivity = new SoftReference<>((AppCompatActivity) activity);
                if (activity instanceof MainActivity && showFormatSdcard) {
                    sdcardFormatHelper.showFormatModal(topActivity.get());
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    private SoftReference<AppCompatActivity> topActivity;
    private SdcardFormatHelper sdcardFormatHelper;
    private final BroadcastReceiver mScreeOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Activity activity = topActivity.get();
                if (!(activity instanceof UnlockActivity)
                        && Utilities.hasVaultCreated(activity)
                        && !Utilities.isAttackDetected(activity)) {
                    startActivity(new Intent(activity, UnlockActivity.class));
                }
            }
        }
    };
}
