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

package com.keystone.cold.ui.views;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.PasswordInputModalBinding;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.fingerprint.VerifyListener;
import com.keystone.cold.setting.VibratorHelper;
import com.keystone.cold.ui.fragment.PasswordLockFragment;
import com.keystone.cold.ui.fragment.setting.MainPreferenceFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.HashUtil;
import com.keystone.cold.util.KeyStoreUtil;
import com.keystone.cold.util.Keyboard;

import org.spongycastle.util.encoders.Hex;

import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;

import static com.keystone.cold.ui.fragment.PasswordLockFragment.MAX_PWD_RETRY_TIMES;
import static com.keystone.cold.ui.fragment.setting.FingerprintEnrollFragment.SECP256R1;

public class AuthenticateModal {
    public static void show(AppCompatActivity activity,
                            String title, String subTitle,
                            OnVerify onVerify,
                            Runnable onForget) {
        show(activity, title, subTitle, false, onVerify, onForget);
    }

    public static void show(AppCompatActivity activity, String title, String subTitle,
                            boolean useFingerprint, OnVerify onVerify, Runnable onForget) {
        PasswordInputModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.password_input_modal, null, false);
        ModalDialog dialog = ModalDialog.newInstance();
        dialog.setBinding(binding);

        initPassword(activity, title, subTitle, onVerify, onForget, binding, dialog);
        if (useFingerprint) {
            initFingerprint(activity, onVerify, binding, dialog);
        }

        dialog.show(activity.getSupportFragmentManager(), "");
        if (!useFingerprint) {
            Keyboard.show(activity, binding.input);
        }
    }

    private static void initFingerprint(AppCompatActivity activity,
                                        OnVerify onVerify,
                                        PasswordInputModalBinding binding,
                                        ModalDialog dialog) {
        FingerprintManager.CryptoObject cryptoObject = initCryptoObject();
        if (cryptoObject == null) {
            binding.fingerprintLayout.setVisibility(View.GONE);
            binding.passwordLayout.setVisibility(View.VISIBLE);
            Keyboard.show(activity, binding.input);
            return;
        }
        FingerprintKit fpKit = new FingerprintKit(activity);
        fpKit.startVerify(new VerifyListener() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                binding.fingerprintLayout.setVisibility(View.GONE);
                binding.passwordLayout.setVisibility(View.VISIBLE);
                Keyboard.show(activity, binding.input);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.CryptoObject cryptoObject) {
                Signature signature = cryptoObject.getSignature();
                dialog.dismiss();
                if (onVerify != null) {
                    onVerify.onVerify(new OnVerify.VerifyToken(null, signature));
                }
            }
        }, initCryptoObject());
        binding.fingerprintLayout.setVisibility(View.VISIBLE);
        binding.fingerprintLayout.setTag(fpKit);
        binding.passwordLayout.setVisibility(View.GONE);
        binding.switchToPassword.setOnClickListener(v -> {
            binding.fingerprintLayout.setVisibility(View.GONE);
            binding.passwordLayout.setVisibility(View.VISIBLE);
            fpKit.cancelVerify();
            Keyboard.show(activity, binding.input);
        });
    }

    private static FingerprintManager.CryptoObject initCryptoObject() {
        Signature signature;
        try {
            signature = Signature.getInstance("SHA256withECDSA");
            PrivateKey key = (PrivateKey) KeyStoreUtil.prepareKeyStore().getKey(SECP256R1,null);
            signature.initSign(key);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        FingerprintManager.CryptoObject cryptoObject = null;
        cryptoObject = new FingerprintManager.CryptoObject(signature);
        return cryptoObject;
    }

    private static void initPassword(AppCompatActivity activity,
                                     String title, String subTitle,
                                     OnVerify onVerify,
                                     Runnable onForget,
                                     PasswordInputModalBinding binding,
                                     ModalDialog dialog) {
        ObservableField<String> password = new ObservableField<>();
        password.set("");
        binding.setPassword(password);
        binding.title.setText(title);
        if (!TextUtils.isEmpty(subTitle)) {
            binding.subTitle.setText(subTitle);
        } else {
            binding.subTitle.setVisibility(View.GONE);
        }

        Runnable close = () -> {
            Keyboard.hide(activity, binding.input);
            if (binding.fingerprintLayout.getTag() != null) {
                FingerprintKit fp = (FingerprintKit) binding.fingerprintLayout.getTag();
                fp.cancelVerify();
            }
            dialog.dismiss();
        };
        binding.closePassword.setOnClickListener(v -> close.run());
        binding.closeFingerprint.setOnClickListener(v -> close.run());
        binding.confirm.setOnClickListener(v -> {

            Handler handler = new Handler();
            binding.confirm.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
            AppExecutors.getInstance().networkIO().execute(() -> {
                String passwordHash = Hex.toHexString(HashUtil.twiceSha256(password.get()));
                boolean verified = PasswordLockFragment.verifyPassword(password.get());

                if (verified) {
                    FingerprintKit.verifyPassword(activity);
                    handler.post(() -> {
                        Utilities.setPasswordRetryTimes(activity, 0);
                        Utilities.setPatternRetryTimes(activity, 0);
                        dialog.dismiss();
                        Keyboard.hide(activity, binding.input);
                        onVerify.onVerify(new OnVerify.VerifyToken(passwordHash,null));
                    });
                } else {
                    handler.post(() -> {
                        binding.confirm.setVisibility(View.VISIBLE);
                        binding.progress.setVisibility(View.GONE);
                        password.set("");
                        VibratorHelper.vibrate(activity);
                        int retry = Utilities.getPasswordRetryTimes(activity);
                        retry += 1;
                        Utilities.setPasswordRetryTimes(activity, retry);
                        binding.hint.setText(getHintText(retry, activity));
                        binding.hint.setVisibility(View.VISIBLE);
                        if (retry >= MAX_PWD_RETRY_TIMES) {
                            Keyboard.hide(activity, binding.input);
                            handler.postDelayed(() -> MainPreferenceFragment.reset(activity), 1000);
                        }
                    });
                }
            });
        });

        if (onForget == null) {
            binding.forget.setVisibility(View.GONE);
        } else {
            binding.forget.setOnClickListener(v -> {
                dialog.dismiss();
                onForget.run();
            });
        }


    }

    private static String getHintText(int retryTimes, Activity activity) {
        String hint;
        if (retryTimes < MAX_PWD_RETRY_TIMES) {
            hint = activity.getString(R.string.password_error_hint,
                    MAX_PWD_RETRY_TIMES - retryTimes);
        } else {
            hint = activity.getString(R.string.reset_hint);
        }
        return hint;
    }

    public interface OnVerify {
        class VerifyToken {
            public String password;
            public Signature signature;

            public VerifyToken(String password, Signature signature) {
                this.password = password;
                this.signature = signature;
            }

            public static void invalid(VerifyToken token) {
                token.password = null;
                token.signature = null;
                token = null;
            }
        }
        void onVerify(VerifyToken token);
    }
}
