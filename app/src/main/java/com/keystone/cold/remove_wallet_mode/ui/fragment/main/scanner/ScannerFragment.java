package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentScannerBinding;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.UnknownException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor.ProcessorManager;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.CaptureHandler;
import com.keystone.cold.ui.fragment.main.scan.scanner.Host;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.bean.ZxingConfig;
import com.keystone.cold.ui.fragment.main.scan.scanner.bean.ZxingConfigBuilder;
import com.keystone.cold.ui.fragment.main.scan.scanner.camera.CameraManager;
import com.sparrowwallet.hummingbird.UR;

import java.io.IOException;

public class ScannerFragment extends BaseFragment<FragmentScannerBinding> implements SurfaceHolder.Callback, Host {
    public static final String TAG = "ScannerFragment";
    private CameraManager mCameraManager;
    private CaptureHandler captureHandler;
    private boolean hasSurface;
    private ZxingConfig mConfig;
    private SurfaceHolder mSurfaceHolder;

    private ObjectAnimator scanLineAnimator;

    @Override
    protected int setView() {
        return R.layout.fragment_scanner;
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

    @Override
    public void handleDecode(String text) {
        try {
            Destination destination = ProcessorManager.handleScanResult(new ScanResult(ScanResultTypes.PLAIN_TEXT, text));
            handleNavigation(destination);
        } catch (Exception e) {
            displayException(e);
        }
    }

    @Override
    public void handleDecode(UR ur) {
        try {
            Destination destination = ProcessorManager.handleScanResult(new ScanResult(ur));
            handleNavigation(destination);
        } catch (Exception e) {
            displayException(e);
        }
    }

    @Override
    public void handleDecode(SubstratePayload substratePayload) {
        // TODO: remove this function because there is no usage.
        try {
            Destination destination = ProcessorManager.handleScanResult(new ScanResult(ScanResultTypes.UOS, substratePayload.rawData));
            handleNavigation(destination);
        } catch (Exception e) {
            displayException(e);
        }
    }

    @Override
    public void handleUOS(String uosHex) {
        try {
            Destination destination = ProcessorManager.handleScanResult(new ScanResult(ScanResultTypes.UOS, uosHex));
            handleNavigation(destination);
        } catch (Exception e) {
            displayException(e);
        }
    }

    private void displayException(Exception e) {
        if (e instanceof BaseException) {
            alertException((BaseException) e, reset);
        } else {
            displayException(UnknownException.newInstance(e));
        }
    }

    private void handleNavigation(Destination destination) {
        navigateUp();
        navigate(destination.id, destination.bundle);
    }


    private Runnable reset = () -> {
        mBinding.scanProgress.setText("");
        if (captureHandler != null) {
            captureHandler.restartPreviewAndDecode();
        }
    };

    @Override
    public void handleProgress(int total, int scan) {
        mActivity.runOnUiThread(() -> mBinding.scanProgress.setText(getString(R.string.scan_progress, scan + "/" + total)));
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
