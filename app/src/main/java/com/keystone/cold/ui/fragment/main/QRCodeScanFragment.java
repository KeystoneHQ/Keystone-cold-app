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

package com.keystone.cold.ui.fragment.main;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.polkadot.UOS.Extrinsic;
import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.coinlib.exception.CoinNotFindException;
import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.exception.InvalidUOSException;
import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.QrcodeScanFragmentBinding;
import com.keystone.cold.scan.CaptureHandler;
import com.keystone.cold.scan.Host;
import com.keystone.cold.scan.QREncoding;
import com.keystone.cold.scan.bean.ZxingConfig;
import com.keystone.cold.scan.bean.ZxingConfigBuilder;
import com.keystone.cold.scan.camera.CameraManager;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.tx.PolkadotJsTxConfirmViewModel;
import com.keystone.cold.viewmodel.QrScanViewModel;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;

import org.json.JSONException;

import java.io.IOException;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;

public class QRCodeScanFragment extends BaseFragment<QrcodeScanFragmentBinding>
        implements SurfaceHolder.Callback, Host {

    private CameraManager mCameraManager;
    private CaptureHandler mHandler;
    private boolean hasSurface;
    private ZxingConfig mConfig;
    private SurfaceHolder mSurfaceHolder;

    private String purpose;

    private QrScanViewModel viewModel;
    private ModalDialog dialog;

    private ObjectAnimator scanLineAnimator;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.qrcode_scan_fragment;
    }

    @Override
    protected void init(View view) {
        boolean inSetupProcess = getArguments() != null && getArguments().getBoolean(IS_SETUP_VAULT);
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        purpose = getArguments() != null ? getArguments().getString("purpose") : "";
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mConfig = new ZxingConfigBuilder()
                .setIsFullScreenScan(true)
                .setFrameColor(R.color.colorAccent)
                .createZxingConfig();
        mCameraManager = new CameraManager(mActivity, mConfig);
        mBinding.frameView.setCameraManager(mCameraManager);
        mBinding.frameView.setZxingConfig(mConfig);
        QrScanViewModel.Factory factory = new QrScanViewModel.Factory(mActivity.getApplication(), inSetupProcess);
        viewModel = ViewModelProviders.of(this, factory).get(QrScanViewModel.class);


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
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
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
            if (mHandler == null) {
                mHandler = new CaptureHandler(this, mCameraManager, watchWallet.getQrEncoding());
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
    public void handleDecode(String res, QREncoding encoding) {
        switch (encoding) {
            case UR:
                handleUR(res);
                break;
            case UOS:
                handleUOS(res);
                break;
            case PLAINTEXT:
                handlePlainText(res);
                break;
        }
    }

    private void handlePlainText(String res) {
        Log.w(TAG, "handlePlainText: " + res);
        if ("webAuth".equals(purpose)) {
            alert(getString(R.string.invalid_webauth_qrcode_hint));
        } else {
            alert(getString(R.string.unresolve_tx),
                    getString(R.string.unresolve_tx_hint, watchWallet.getWalletName(mActivity)));
        }
    }

    private void handleUOS(String res) {
        try {
            SubstratePayload sp = new SubstratePayload(res);
            handlePolkadotJsTransaction(res, sp);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleUR(String res) {
        try {
            viewModel.handleUrQrCode(this, res);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void handleException(Exception e) {
        e.printStackTrace();
        dismissLoading();
        if (e instanceof InvalidTransactionException || e instanceof InvalidUOSException) {
            alert(getString(R.string.unresolve_tx),
                    getString(R.string.unresolve_tx_hint,
                            WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
        } else if (e instanceof JSONException) {
            alert(getString(R.string.incorrect_qrcode));
        } else if (e instanceof CoinNotFindException) {
            alert( getString(R.string.unsupported_coin));
        } else if (e instanceof UnknowQrCodeException) {
            alert(getString(R.string.unsupported_qrcode));
        } else if (e instanceof InvalidAccountException | e instanceof XfpNotMatchException ) {
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.account_not_match),
                    getString(R.string.account_not_match_detail) ,
                    getString(R.string.confirm),
                    this::rescan);
        }
    }

    protected void handlePolkadotJsTransaction(String res, SubstratePayload sp) {
        PolkadotJsTxConfirmViewModel viewModel = ViewModelProviders.of(this)
                .get(PolkadotJsTxConfirmViewModel.class);
        Extrinsic extrinsic = sp.extrinsic;
        if (!viewModel.isNetworkSupported(sp.network)) {
            alert(getString(R.string.unknown_substrate_chain_title),
                    getString(R.string.unknown_substrate_chain_content));
        } else if (extrinsic == null ||
                !viewModel.isTransactionSupported(extrinsic.palletParameter)) {
            alert(getString(R.string.unsupported_polka_tx_type_title),
                    getString(R.string.unsupported_polka_tx_type_content));
        } else if (!viewModel.isAccountMatch(sp.getAccount())) {
            alert(getString(R.string.account_not_match),
                    getString(R.string.account_not_match_detail));
        } else {
            handlePolkadotJsTx(res);
        }
    }

    private void handlePolkadotJsTx(String res) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_DATA, res);
        bundle.putBoolean("substrateTx", true);
        navigate(R.id.action_to_polkadotTxConfirm, bundle);
    }

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
        return mHandler;
    }

    public void alert(String message) {
        alert(null, message);
    }

    public void alert(String title, String message) {
        alert(title, message, null);
    }

    public void alert(String title, String message, Runnable run) {
        dialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        if (title != null) {
            binding.title.setText(title);
        } else {
            binding.title.setText(R.string.invalid_data);
        }
        binding.subTitle.setText(message);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (run != null) {
                run.run();
            } else {
                rescan();
            }
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "scan fail");
    }

    private void rescan() {
        mBinding.scanProgress.setText("");
        if (mHandler != null) {
            mHandler.restartPreviewAndDecode();
        }
    }
}


