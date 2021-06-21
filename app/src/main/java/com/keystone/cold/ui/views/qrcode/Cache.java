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

package com.keystone.cold.ui.views.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.update.utils.Digest;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Cache {
    private static final String TAG = "Vault.Qrcode.Cache";
    private static final Cache INSTANCE = new Cache();
    private final Paint mPaint;
    private final LruCache<String, Bitmap> mBitmapCache;
    private ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    private final Map<EncodeHintType, Object> mHint;

    private Cache() {
        mHint = new EnumMap<>(EncodeHintType.class);
        mHint.put(EncodeHintType.MARGIN, 0);
        mHint.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(0.5F);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.BLACK);
        mBitmapCache = new LruCache<>(10);
    }

    public static Cache getInstance() {
        return INSTANCE;
    }

    public void offer(@NonNull String msg, @NonNull QrCodeHolder view) {
        final String hash = checksum(msg);
        final Bitmap cached = mBitmapCache.get(hash);

        if (cached != null) {
            renderBitmap(cached, view);
            return;
        }

        mExecutors.submit(() -> {
            try {
                final int width = view.getViewWidth();
                final int height = view.getViewHeight();
                final QRCode qrCode = Encoder.encode(msg, ErrorCorrectionLevel.L, mHint);
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                final Canvas canvas = new Canvas(bitmap);
                renderQrCode(qrCode.getMatrix(), width, height, canvas);
                mBitmapCache.put(hash, bitmap);
                renderBitmap(bitmap, view);
            } catch (Exception e) {
                Log.w(TAG, "Error while setting code", e);
            }
        });
    }

    private void renderBitmap(@NonNull Bitmap bitmap, @NonNull QrCodeHolder view) {
        view.onRender(bitmap);
    }

    private void renderQrCode(@NonNull ByteMatrix matrix, int width, int height, @NonNull Canvas canvas) {
        final int inputWidth = matrix.getWidth();
        final int inputHeight = matrix.getHeight();
        final float cellSize = Math.min(((float) width) / inputWidth, ((float) height / inputHeight));

        float outputY = 0F;
        float outputX;

        canvas.drawColor(Color.WHITE);
        for (int inputY = 0; inputY < inputHeight; ++inputY, outputY += cellSize) {
            outputX = 0F;
            for (int inputX = 0; inputX < inputWidth; ++inputX, outputX += cellSize) {
                if (matrix.get(inputX, inputY) == 1) {
                    canvas.drawRect(outputX, outputY, outputX + cellSize, outputY + cellSize, mPaint);
                }
            }
        }
    }

    private String checksum(String msg) {
        return ByteFormatter.bytes2hex(Digest.MD5.checksum(msg));
    }

    public void restart() {
        mExecutors.shutdownNow();
        mExecutors = Executors.newSingleThreadExecutor();
        mBitmapCache.evictAll();
    }

    public void clear() {
        mExecutors.shutdownNow();
        mBitmapCache.evictAll();
    }
}