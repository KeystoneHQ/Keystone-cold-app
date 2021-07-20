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

package com.keystone.cold.ui.fragment.main.scan.scanner;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.ScannerFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.bean.ZxingConfig;
import com.keystone.cold.ui.fragment.main.scan.scanner.bean.ZxingConfigBuilder;
import com.keystone.cold.ui.fragment.main.scan.scanner.camera.CameraManager;
import com.sparrowwallet.hummingbird.UR;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

public class ScannerFragment extends BaseFragment<ScannerFragmentBinding>
        implements SurfaceHolder.Callback, Host {
    public static final String TAG = "ScannerFragment";
    private CameraManager mCameraManager;
    private CaptureHandler captureHandler;
    private boolean hasSurface;
    private ZxingConfig mConfig;
    private SurfaceHolder mSurfaceHolder;
    private List<ScanResultTypes> desiredTypes;

    private ObjectAnimator scanLineAnimator;

    private ScannerViewModel scannerViewModel;
    private ScannerState scannerState;

    private final Handler handler = new Handler();

    @Override
    protected int setView() {
        return R.layout.scanner_fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerViewModel.reset();
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mConfig = new ZxingConfigBuilder()
                .setIsFullScreenScan(true)
                .setFrameColor(R.color.colorAccent)
                .createZxingConfig();
        mCameraManager = new CameraManager(mActivity, mConfig);
        mBinding.frameView.setCameraManager(mCameraManager);
        mBinding.frameView.setZxingConfig(mConfig);
        scanLineAnimator = ObjectAnimator.ofFloat(mBinding.scanLine, "translationY", 0, 600);
        scanLineAnimator.setDuration(2000L);
        scanLineAnimator.setRepeatCount(ValueAnimator.INFINITE);

        scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerState = scannerViewModel.getState();
        if (scannerState == null) {
            throw new InvalidParameterException("No ScannerState found when initial ScannerFragment");
        }
        scannerState.bindFragment(this);
        scannerState.style(this.mBinding);
        desiredTypes = scannerState.getDesiredResults();

        if (desiredTypes == null) {
            throw new InvalidParameterException("no desired type passed to scanner");
        }
        mBinding.scanHint.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceHolder = mBinding.preview.getHolder();
        if (hasSurface) {
            initCamera(mSurfaceHolder);
        } else {
            mSurfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (captureHandler != null) {
            captureHandler.quitSynchronously();
            captureHandler = null;
        }
        mCameraManager.closeDriver();

        if (!hasSurface) {
            mSurfaceHolder.removeCallback(this);
        }
        scanLineAnimator.cancel();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        scanLineAnimator.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }

    private void initCamera(@NonNull SurfaceHolder surfaceHolder) {
        if (mCameraManager.isOpen()) {
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            if (captureHandler == null) {
                captureHandler = new CaptureHandler(this, mCameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    @Override
    public ZxingConfig getConfig() {
        return mConfig;
    }

    private void runInSubThread(Runnable runnable) {
        AppExecutors.getInstance().diskIO().execute(runnable);
    }

    @Override
    public void navigateUp() {
        handler.post(super::navigateUp);
    }

    @Override
    public void navigate(int id) {
        handler.post(() -> super.navigate(id));
    }

    @Override
    public void navigate(int id, Bundle data) {
        handler.post(() -> super.navigate(id, data));
    }

    @Override
    public void handleDecode(String text) {
        runInSubThread(() -> {
            try {
                if (this.desiredTypes.stream().anyMatch(dt -> dt.isType(text))) {
                    scannerState.handleScanResult(new ScanResult(ScanResultTypes.PLAIN_TEXT, text));
                } else {
                    alert(getString(R.string.scan_failed), getString(R.string.unsupported_qrcode));
                }
            } catch (Exception e) {
                if (!scannerState.handleException(e)) {
                    alert(getString(R.string.scan_failed), getString(R.string.unsupported_qrcode));
                }
            }
        });
    }

    @Override
    public void handleDecode(UR ur) {
        runInSubThread(() -> {
            try {
                ScanResultTypes srt = this.desiredTypes.stream().filter(dt -> dt.isType(ur)).findFirst().orElse(null);
                if (srt != null) {
                    scannerState.handleScanResult(new ScanResult(srt, Hex.toHexString(ur.getCborBytes())));
                } else {
                    alert(getString(R.string.scan_failed), getString(R.string.unsupported_qrcode));
                }
            } catch (Exception e) {
                if (!scannerState.handleException(e)) {
                    alert(getString(R.string.scan_failed), getString(R.string.unsupported_qrcode));
                }
            }
        });
    }

    @Override
    public void alert(String message) {
        alert(null, message);
    }

    @Override
    public void alert(String title, String message) {
        alert(title, message, null);
    }

    @Override
    public void alert(String title, String message, Runnable run) {
        handler.post(() -> {
            super.alert(title, message, () -> {
                if (run != null) {
                    run.run();
                } else {
                    mBinding.scanProgress.setText("");
                    if (captureHandler != null) {
                        captureHandler.restartPreviewAndDecode();
                    }
                }
            });
        });
    }


    @Override
    public void handleProgressPercent(double percent) {
        mActivity.runOnUiThread(() -> mBinding.scanProgress.setText(getString(R.string.scan_progress, (int) Math.floor((percent * 100)) + "%")));
    }

    @Override
    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public Handler getHandler() {
        return captureHandler;
    }

}


