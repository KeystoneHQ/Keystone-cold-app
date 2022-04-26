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

import static android.content.Context.MODE_PRIVATE;
import static com.keystone.cold.ui.fragment.main.web3.EthTxConfirmFragment.PREFERENCE_KEY_VISITS;
import static com.keystone.cold.ui.fragment.setting.FingerprintPreferenceFragment.FINGERPRINT_UNLOCK;
import static com.keystone.cold.viewmodel.SetupVaultViewModel.VAULT_CREATE_STEP;
import static com.keystone.cold.viewmodel.SetupVaultViewModel.VAULT_CREATE_STEP_WELCOME;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.ui.modal.ModalDialog;

public class Utilities {
    public static final String PREFERENCE_SECRET = "secret";
    public static final String PREFERENCE_KEY_PASSWORD = "password";
    public static final String PREFERENCE_KEY_PATTERN = "pattern";
    public static final String PREFERENCE_KEY_VAULT_CREATED = "vault_created";
    public static final String PREFERENCE_KEY_PASSWORD_SET = "password_set";
    public static final String PREFERENCE_KEY_LANGUAGE_SET = "language_set";
    public static final String PREFERENCE_KEY_VAULT_ID = "vault_id";

    public static final String SHARED_PREFERENCES_KEY = "com.keystone.cold.prefs";

    public static final String IS_SETUP_VAULT = "is_setup_vault";
    public static final String IS_SWITCH_WATCH_WALLET = "is_switch_wallet";
    public static final String IS_SET_PASSPHRASE = "is_set_passphrase";
    public static final String PREFERENCE_KEY_BELONG_TO = "belong_to_v2";
    public static final String PREFERENCE_KEY_LEGACY_BELONG_TO = "belong_to";
    public static final String PREFERENCE_KEY_PWD_RETRY = "pwd_retry_times";
    public static final String PREFERENCE_KEY_PATTERN_RETRY = "pattern_retry_times";
    public static final String FINGERPRINT_CLICKED = "fingerprint_clicked";
    public static final String PATTERN_LOCK_CLICKED = "pattern_lock_clicked";
    public static final String PREFERENCE_KEY_RANDOM_SALT = "random_salt";
    public static final String FINGERPRINT_PASSWORD = "fingerprint_password";
    public static final String ATTACK_DETECTED = "attack_detected";
    public static final String INPUT_SETTINGS_CLEARED = "input_settings_cleared";
    public static final String ETH_CURRENT_ACCOUNT = "eth_current_account";
    public static final String SOL_CURRENT_ACCOUNT = "sol_current_account";
    public static final String WEB3_GUIDE_TIMES = "web3_guide_times";

    public static final String NFT_AVATAR_RESOURCE = "nft_avatar_resource";

    public static final String PREFERENCE_ETH_KEY_SYNCED = "user_click_eth_sync";
    public static final String PREFERENCE_SOL_KEY_SYNCED = "user_click_sol_sync";
    public static final String SOL_DERIVATION_PATHS = "sol_derivation_paths";


    public static void alert(AppCompatActivity activity,
                             @Nullable String title, @NonNull String message,
                             String buttonText, Runnable action) {
        ModalDialog.showCommonModal(activity,
                title,
                message,
                buttonText,
                action);
    }

    public static boolean hasVaultCreated(Context activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_VAULT_CREATED, false);
    }

    public static void setVaultCreated(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_VAULT_CREATED, true).apply();
    }

    public static boolean hasPasswordSet(Context activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_PASSWORD_SET, false);
    }

    public static void markPasswordSet(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_PASSWORD_SET, true).apply();
    }

    public static void clearPasswordSet(Activity context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_PASSWORD_SET, false).apply();
    }

    public static void setLanguageSet(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_LANGUAGE_SET, true).apply();
    }

    public static boolean hasLanguageSet(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_LANGUAGE_SET, false);
    }

    public static boolean isPatternUnlock(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return !TextUtils.isEmpty(sp.getString(PREFERENCE_KEY_PATTERN, ""));
    }

    public static boolean verifyPatternUnlock(Activity activity, String patternSha1) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return patternSha1.equals(sp.getString(PREFERENCE_KEY_PATTERN, ""));
    }

    public static void setPattern(Activity activity, String s) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_PATTERN, s).apply();
    }

    public static void clearPatternUnlock(FragmentActivity activity) {
        setPattern(activity, "");
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
    }

    public static String getVaultId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_VAULT_ID, "");
    }

    public static void setVaultId(Context context, String id) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_VAULT_ID, id).apply();
    }

    public static void setRandomSalt(Context context, String salt) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_RANDOM_SALT, salt).apply();
    }

    public static String getRandomSalt(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_RANDOM_SALT, "");
    }

    public static void setCurrentBelongTo(Context context, String s) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_BELONG_TO, s).apply();
    }

    public static String getCurrentBelongTo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_BELONG_TO, "main");
    }

    public static void setPasswordRetryTimes(Context context, int times) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_PWD_RETRY, times).apply();
    }

    public static int getPasswordRetryTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_PWD_RETRY, 0);
    }

    public static void setPatternRetryTimes(Context context, int times) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_PATTERN_RETRY, times).apply();
    }

    public static int getPatternRetryTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_PATTERN_RETRY, 0);
    }

    public static boolean isFingerprintUnlockEnable(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(FINGERPRINT_UNLOCK, false);
    }

    public static void setFingerprintUnlockEnable(Context context, boolean enable) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(FINGERPRINT_UNLOCK, enable).apply();
    }

    public static boolean hasUserClickFingerprint(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(FINGERPRINT_CLICKED, false);
    }

    public static void setUserClickFingerprint(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(FINGERPRINT_CLICKED, true).apply();
    }

    public static boolean hasUserClickPatternLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PATTERN_LOCK_CLICKED, false);
    }

    public static void setUserClickPatternLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PATTERN_LOCK_CLICKED, true).apply();
    }

    public static boolean hasUserClickEthSyncLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_ETH_KEY_SYNCED, false);
    }

    public static void setUserClickEthSyncLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_ETH_KEY_SYNCED, true).apply();
    }


    public static boolean hasUserClickSolSyncLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_SOL_KEY_SYNCED, false);
    }

    public static void setUserClickSolSyncLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_SOL_KEY_SYNCED, true).apply();
    }


    public static String getFingerprintPassword(Context context) {
        return Settings.System.getString(context.getContentResolver(), FINGERPRINT_PASSWORD);
    }

    public static boolean setFingerprintPassword(Context context, String pwd) {
        return Settings.System.putString(context.getContentResolver(), FINGERPRINT_PASSWORD, pwd);
    }

    public static void setAttackDetected(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(ATTACK_DETECTED, true).apply();
    }

    public static void setAttackDetected(Context context, boolean attached) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(ATTACK_DETECTED, attached).apply();
    }

    public static boolean isAttackDetected(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(ATTACK_DETECTED,false);
    }

    public static int getVisitsTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_VISITS, 0);
    }

    public static void setVisitsTimes(Context context, int visits) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_VISITS, visits).apply();
    }

    public static void setVaultCreateStep(Context context, int step) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putInt(VAULT_CREATE_STEP, step).apply();
    }

    public static Integer getVaultCreateStep(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getInt(VAULT_CREATE_STEP, VAULT_CREATE_STEP_WELCOME);
    }

    public static void setWeb3GuideTimes(Context context, int times) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putInt(WEB3_GUIDE_TIMES, times).apply();
    }

    public static int getWeb3GuideTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getInt(WEB3_GUIDE_TIMES, 0);
    }

    public static void setCurrentEthAccount(Context context, String code) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putString(ETH_CURRENT_ACCOUNT, code).apply();
    }

    public static String getCurrentEthAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getString(ETH_CURRENT_ACCOUNT, ETHAccount.BIP44_STANDARD.getCode());
    }

    public static void setCurrentSolAccount(Context context, String code) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putString(SOL_CURRENT_ACCOUNT, code).apply();
    }

    public static String getCurrentSolAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getString(SOL_CURRENT_ACCOUNT, SOLAccount.SOLFLARE_BIP44.getCode());
    }

    public static void setSolDerivationPaths(Context context, String paths) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putString(SOL_DERIVATION_PATHS, paths).apply();
    }

    public static String getSolDerivationPaths(Context context){
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getString(SOL_DERIVATION_PATHS, "");
    }

    public static void setNftAvatarResource(Context context, String mediaData) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putString(NFT_AVATAR_RESOURCE, mediaData).apply();
    }

    public static String getNftAvatarResource(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getString(NFT_AVATAR_RESOURCE, null);
    }

    public static Bitmap getNFTAvatarBitmap(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        String mediaData = sp.getString(NFT_AVATAR_RESOURCE, null);
        if(mediaData != null) {
            byte[] decodedImage = Base64.decode(mediaData, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        }else {
            return null;
        }
    }
}
