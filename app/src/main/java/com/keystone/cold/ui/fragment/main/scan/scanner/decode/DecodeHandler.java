/*
 * Copyright (C) 2010 ZXing authors
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

package com.keystone.cold.ui.fragment.main.scan.scanner.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.keystone.cold.ui.fragment.main.scan.scanner.Host;
import com.keystone.cold.ui.fragment.main.scan.scanner.common.Constant;

import java.util.Map;
import java.util.Objects;

public final class DecodeHandler extends Handler {

    private static final String TAG = "Vault.Qrcode.DecodeHandler";

    private final Host host;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(Host host, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.host = host;
    }

    @Override
    public void handleMessage(@NonNull Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case Constant.DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Constant.QUIT:
                running = false;
                Objects.requireNonNull(Looper.myLooper()).quit();
                break;
        }
    }

    private void decode(byte[] data, int width, int height) {

        Result rawResult = null;
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        data = rotatedData;

        PlanarYUVLuminanceSource source = host.getCameraManager()
                .buildLuminanceSource(data, width, height);


        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException ignored) {
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = host.getHandler();
        if (rawResult != null) {

            if (handler != null) {
                Message message = Message.obtain(handler,
                        Constant.DECODE_SUCCEEDED, rawResult);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, Constant.DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }

}
