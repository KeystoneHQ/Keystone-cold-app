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

package com.keystone.cold.fingerprint;

import android.annotation.UserIdInt;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.internal.widget.LockPatternUtils;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.util.HashUtil;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Context.FINGERPRINT_SERVICE;

public class FingerprintKit {

    private static final String TAG = "Vault.FingerprintKit";
    private FingerprintManager fp;
    private final Context mContext;
    private final LockPatternUtils lockPatternUtils;
    @UserIdInt
    private final int mUserId;
    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private CancellationSignal mCancellationSignal;

    private boolean isEnrolling;
    private boolean isVerifying;

    public FingerprintKit(Context context) {
        mContext = context;
        lockPatternUtils = new LockPatternUtils(mContext);
        mUserId = UserHandle.myUserId();
        fp = (FingerprintManager) mContext.getSystemService(FINGERPRINT_SERVICE);
        if (!lockPatternUtils.isLockPasswordEnabled(mUserId)) {
            String password = HashUtil.generateRandomPassword(20);
            if (Utilities.setFingerprintPassword(mContext, password)) {
                lockPatternUtils.saveLockPassword(password, null,
                        DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, mUserId);
            }
        }
    }

    public static boolean isHardwareDetected(Context context) {
        FingerprintManager fm = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
        return fm.isHardwareDetected();
    }

    public static void verifyPassword(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String password = Utilities.getFingerprintPassword(context);
                if (!TextUtils.isEmpty(password)) {
                    new LockPatternUtils(context).verifyPassword(password,
                            0L, UserHandle.myUserId());
                }
            } catch (LockPatternUtils.RequestThrottledException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Fingerprint> getEnrolledFingerprints() {
        Objects.requireNonNull(fp, "fp is null");
        return fp.getEnrolledFingerprints();
    }

    public boolean hasEnrolledFingerprint() {
        Objects.requireNonNull(fp, "fp is null");
        return fp.hasEnrolledFingerprints();
    }

    public void renameFingerprint(@NonNull Fingerprint fingerprint, @NonNull String newName) {
        fp.rename(fingerprint.getFingerId(), UserHandle.myUserId(), newName);
    }

    public void removeFingerprint(@NonNull Fingerprint fingerprint, RemovalListener listener) {
        fp.remove(fingerprint, UserHandle.myUserId(), new FingerprintManager.RemovalCallback() {
            @Override
            public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                super.onRemovalError(fp, errMsgId, errString);
                if (listener != null) {
                    listener.onError(errMsgId, errString.toString());
                }
            }

            @Override
            public void onRemovalSucceeded(Fingerprint fp, int remaining) {
                super.onRemovalSucceeded(fp, remaining);
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    public void cancelEnroll() {
        if (isEnrolling
                && mCancellationSignal != null
                && !mCancellationSignal.isCanceled()) {
            mCancellationSignal.cancel();
            isEnrolling = false;
        }
    }

    public void startEnroll(@Nullable EnrollListener listener) {
        long challenge = fp.preEnroll();
        final Future<byte[]> future = sExecutor.submit(()
                -> lockPatternUtils.verifyPassword(Utilities.getFingerprintPassword(mContext),
                challenge, mUserId));
        byte[] token;
        try {
            token = future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        mCancellationSignal = new CancellationSignal();
        fp.setActiveUser(mUserId);
        isEnrolling = true;
        fp.enroll(token, mCancellationSignal, 0, mUserId, new FingerprintManager.EnrollmentCallback() {
            @Override
            public void onEnrollmentError(int errMsgId, CharSequence errString) {
                super.onEnrollmentError(errMsgId, errString);
                if (listener != null) {
                    listener.onEnrollmentError(errMsgId, errString);
                }
                mCancellationSignal.cancel();
                isEnrolling = false;
            }

            @Override
            public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
                super.onEnrollmentHelp(helpMsgId, helpString);
                if (listener != null) {
                    listener.onEnrollmentHelp(helpMsgId, helpString);
                }
            }

            @Override
            public void onEnrollmentProgress(int remaining) {
                super.onEnrollmentProgress(remaining);
                if (listener != null) {
                    listener.onEnrollmentProgress(remaining);
                }
                if (remaining == 0) {
                    fp.postEnroll();
                    isEnrolling = false;
                }
            }
        });
    }

    public void cancelVerify() {
        if (isVerifying
                && mCancellationSignal != null
                && !mCancellationSignal.isCanceled()) {
            mCancellationSignal.cancel();
            isVerifying = false;
        }
    }

    public void startVerify(@NonNull VerifyListener listener, FingerprintManager.CryptoObject object) {

        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        mCancellationSignal = new CancellationSignal();
        isVerifying = true;
        Log.w(TAG, "fp kit startVerify");
        fp.authenticate(object, mCancellationSignal, 0,
                new FingerprintManager.AuthenticationCallback() {
                    int failCount = 0;
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        listener.onAuthenticationError(errorCode, errString);
                        isVerifying = false;
                        mCancellationSignal.cancel();
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        listener.onAuthenticationSucceeded(result.getCryptoObject());
                        isVerifying = false;
                        mCancellationSignal.cancel();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        failCount++;
                        if (failCount == 5) {
                            onAuthenticationError(0, "");
                        }
                    }

                    @Override
                    public void onAuthenticationAcquired(int acquireInfo) {
                    }
                }, null, mUserId);

    }

}


