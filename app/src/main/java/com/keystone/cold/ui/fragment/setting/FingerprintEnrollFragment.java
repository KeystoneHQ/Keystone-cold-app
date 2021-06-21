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

package com.keystone.cold.ui.fragment.setting;

import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.keystone.coinlib.Util;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.RegisterPublicKeyCallable;
import com.keystone.cold.databinding.FingerprintEnrollBinding;
import com.keystone.cold.databinding.InputModalBinding;
import com.keystone.cold.fingerprint.EnrollListener;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.fingerprint.RemovalListener;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.KeyStoreUtil;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.util.List;
import java.util.Objects;

import static com.keystone.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;
import static com.keystone.cold.util.KeyStoreUtil.KEYSTORE_PROVIDER_NAME;

public class FingerprintEnrollFragment extends BaseFragment<FingerprintEnrollBinding>
        implements EnrollListener {

    private FingerprintKit fingerprintKit;
    private static final int ENROLL_TIMES = 12;

    private Fingerprint currentEnrolled;
    private final ObservableField<String> input = new ObservableField<>();
    private boolean enrollSuccess;
    private ModalDialog dialog;
    public static final String SECP256R1 = "FINGERPRINT_SECP256_R1";
    private String password;

    @Override
    protected int setView() {
        return R.layout.fingerprint_enroll;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle bundle = getArguments();
        password = Objects.requireNonNull(bundle).getString(PASSWORD);
        prepareEnrollment();
    }

    private void prepareEnrollment() {
        fingerprintKit = new FingerprintKit(mActivity);
        fingerprintKit.startEnroll(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }

    @Override
    public void onEnrollmentError(int errMsgId, CharSequence errString) {
        mBinding.enrollHelp.setText(errString);
    }

    @Override
    public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        mBinding.enrollHelp.setText(helpString);
    }

    @Override
    public void onEnrollmentProgress(int remaining) {
        int progress = (int) (((float) ENROLL_TIMES - remaining) / ENROLL_TIMES * 100);
        mBinding.progress.setProgressWithAnimation(progress, 300);
        mBinding.percent.setText(progress + "%");
        mBinding.enrollHelp.setText("");
        if (remaining == 0) {
            onEnrollComplete();
            enrollSuccess = true;
        }
    }

    private void onEnrollComplete() {
        List<Fingerprint> list = fingerprintKit.getEnrolledFingerprints();
        currentEnrolled = list.get(list.size() - 1);
        input.set(currentEnrolled.getName().toString());
        mBinding.enrolling.setVisibility(View.GONE);
        mBinding.complete.setVisibility(View.VISIBLE);
        mBinding.confirm.setOnClickListener(v -> navigateUp());
        mBinding.hint.setText(getString(R.string.enroll_complete_hint, currentEnrolled.getName()));
        mBinding.rename.setOnClickListener(v -> rename());
        if (!generateKeyPair()) {
            fingerprintKit.removeFingerprint(currentEnrolled, new RemovalListener() {
                @Override
                public void onSuccess() {
                    navigateUp();
                }

                @Override
                public void onError(int errMsgId, String errString) {
                    navigateUp();
                }
            });
        } else {
            int retry = 0;
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_NAME);
                keyStore.load(null);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
            while (retry < 5) {
                boolean registerResult = registerPublicKey(keyStore);
                if (registerResult) {
                    break;
                }
                retry++;
            }
            if (list.size() == 1) {
                Utilities.setFingerprintUnlockEnable(mActivity, true);
            }
        }
    }

    private boolean generateKeyPair() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_NAME);
            keyStore.load(null);
            PrivateKey key = (PrivateKey) keyStore.getKey(SECP256R1, null);

            //we use single key for multi fingerprint, every time enroll new fingerprint,
            // check if the key is valid
            if (key == null) {
                KeyStoreUtil.generateSecp256r1InKeyStore(SECP256R1, true);
            }

            //if key is invalid ,regenerate the key
            if (checkKey(keyStore)) {
                return true;
            } else {
                keyStore.deleteEntry(SECP256R1);
                KeyStoreUtil.generateSecp256r1InKeyStore(SECP256R1, true);
                if (checkKey(keyStore)) {
                    return true;
                } else {
                    keyStore.deleteEntry(SECP256R1);
                    return false;
                }
            }

        } catch (KeyStoreException | CertificateException | RuntimeException
                | NoSuchAlgorithmException | IOException
                | UnrecoverableKeyException e) {
            e.printStackTrace();
            Log.e("Vault.Fingerprint", "Exception ", e);
        }
        return false;
    }

    private boolean checkKey(KeyStore keyStore) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            PrivateKey key = (PrivateKey) keyStore.getKey(SECP256R1, null);
            if (key == null) return false;
            signature.initSign(key);
            return true;
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException
                | KeyStoreException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerPublicKey(KeyStore keyStore) {
        if (!TextUtils.isEmpty(password)) {
            ECPublicKey publicKey;
            try {
                publicKey = (ECPublicKey) keyStore.getCertificate(SECP256R1).getPublicKey();
            } catch (KeyStoreException e) {
                e.printStackTrace();
                return false;
            }
            ECPoint ecPoint = publicKey.getW();
            boolean result = new RegisterPublicKeyCallable(
                    Util.extractPublicKey(ecPoint),
                    requireArguments().getString(PASSWORD)).call();
            Log.w(TAG, "register public key result :" + result);
            return result;
        }
        return false;
    }

    private void rename() {
        dialog = new ModalDialog();
        InputModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.input_modal, null, false);
        binding.title.setText(R.string.fingerprint_rename_subtitle);
        binding.setInput(input);
        binding.inputBox.setSelectAllOnFocus(true);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setOnClickListener(v -> {
            fingerprintKit.renameFingerprint(currentEnrolled, Objects.requireNonNull(input.get()));
            dialog.dismiss();
            navigateUp();
        });
        dialog.setBinding(binding);
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
    }

    @Override
    public void navigateUp() {
        if (fingerprintKit.getEnrolledFingerprints().size() == 1 && enrollSuccess) {
            navigate(R.id.action_enroll_to_fingerprintSettingFragment, getArguments());
        } else {
            super.navigateUp();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fingerprintKit.cancelEnroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
