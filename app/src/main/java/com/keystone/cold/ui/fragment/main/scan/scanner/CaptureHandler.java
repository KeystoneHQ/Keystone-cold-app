/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keystone.cold.ui.fragment.main.scan.scanner;

import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;
import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.coinlib.coins.polkadot.UOS.UOSDecoder;
import com.keystone.coinlib.coins.polkadot.UOS.UosDecodeResult;
import com.keystone.coinlib.exception.InvalidUOSException;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.scan.QREncoding;
import com.keystone.cold.ui.fragment.main.scan.scanner.camera.CameraManager;
import com.keystone.cold.ui.fragment.main.scan.scanner.common.Constant;
import com.keystone.cold.ui.fragment.main.scan.scanner.decode.DecodeThread;
import com.keystone.cold.viewmodel.PolkadotViewModel;
import com.sparrowwallet.hummingbird.ResultType;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.URDecoder;

import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CaptureHandler extends Handler {

    private final Host host;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;
    private URDecoder decoder = new URDecoder();
    private PolkadotViewModel.PolkadotDecoder polkadotDecoder = new PolkadotViewModel.PolkadotDecoder();
    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private enum State {
        PREVIEW, SUCCESS, DONE
    }


    public CaptureHandler(Host host, CameraManager cameraManager) {
        this.host = host;
        decodeThread = new DecodeThread(host);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Constant.RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case Constant.DECODE_SUCCEEDED:
                Result result = (Result) message.obj;
                String text = result.getText();
                String hex = Hex.toHexString(result.getRawBytes());
                if (!text.startsWith("UR") && !text.startsWith("ur")) {
                    if (polkadotDecoder.tryReadFirst(hex)) {
                        polkadotDecode(result);
                        return;
                    }
                }
                tryDecode(text);
                break;
            case Constant.DECODE_FAILED:
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.DECODE);
                break;
            case Constant.RETURN_SCAN_RESULT:
                break;
        }
    }

    private void tryDecode(String text) {
        sExecutor.submit(() -> {
            boolean canReceive;
            try {
                canReceive = decoder.receivePart(text);
            } catch (Exception e) {
                e.printStackTrace();
                canReceive = false;
            }
            if (canReceive) {
                //done scan
                if (decoder.getResult() != null) {
                    if (decoder.getResult().type == ResultType.SUCCESS) {
                        UR result = decoder.getResult().ur;
                        decodeComplete(result);
                    } else {
                        decodeComplete(text);
                    }
                } else {
                    //continue scan
                    state = State.PREVIEW;
                    host.handleProgressPercent(decoder.getEstimatedPercentComplete());
                    cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.DECODE);
                }
                return;
            }
            decodeComplete(text);
        });
    }

    private void polkadotDecode(Result result) {
        sExecutor.submit(() -> {
            boolean canReceive;
            String message = Hex.toHexString(result.getRawBytes());
            try {
                canReceive = polkadotDecoder.receive(message);
            } catch (PolkadotViewModel.PolkadotException e) {
                e.printStackTrace();
                canReceive = false;
            }
            if (canReceive) {
                String decoded;
                try {
                    decoded = polkadotDecoder.decode();
                } catch (PolkadotViewModel.PolkadotException e) {
                    e.printStackTrace();
                    return;
                }
                if (decoded != null) {
                    decodeUOS(decoded);
                } else {
                    state = State.PREVIEW;
                    host.handleProgress(polkadotDecoder.getTotal(), polkadotDecoder.getCurrent());
                    cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.DECODE);
                }
            } else {
                decodeComplete(result.getText());
            }
        });
    }

    private void decodeComplete(String result) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            state = State.SUCCESS;
            host.handleDecode(result);
        });
    }

    private void decodeComplete(UR result) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            state = State.SUCCESS;
            host.handleDecode(result);
        });
    }

    private void decodeUOS(String uosHex) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            state = State.SUCCESS;
            host.handleUOS(uosHex);
        });
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Constant.QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(Constant.DECODE_SUCCEEDED);
        removeMessages(Constant.DECODE_FAILED);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            decoder = new URDecoder();
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.DECODE);
        }
    }

}
