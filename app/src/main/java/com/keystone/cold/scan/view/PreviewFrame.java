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

package com.keystone.cold.scan.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.keystone.cold.R;
import com.keystone.cold.scan.bean.ZxingConfig;
import com.keystone.cold.scan.camera.CameraManager;


public final class PreviewFrame extends View {

    private CameraManager cameraManager;
    private Paint mMaskPaint, mCornerPaint, frameLinePaint;

    private final int mMaskColor;
    private int mCornerColor;
    private int mFrameLineColor = -1;

    private ZxingConfig mConfig;
    private Rect mFrame;
    private int[] mXY;


    public PreviewFrame(Context context) {
        this(context, null);

    }

    public PreviewFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setZxingConfig(@NonNull ZxingConfig config) {
        this.mConfig = config;
        mCornerColor = ContextCompat.getColor(getContext(), config.getFrameColor());

        if (config.getFrameLineColor() != -1) {
            mFrameLineColor = ContextCompat.getColor(getContext(), config.getFrameLineColor());
        }

        initPaint();

    }

    public PreviewFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMaskColor = ContextCompat.getColor(getContext(), R.color.preview_mask);
    }

    private void initPaint() {
        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCornerPaint.setColor(mCornerColor);
        mCornerPaint.setStyle(Paint.Style.FILL);
        mCornerPaint.setStrokeWidth(dp2px(1));

        if (mFrameLineColor != -1) {
            frameLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            frameLinePaint.setColor(ContextCompat.getColor(getContext(), mConfig.getFrameLineColor()));
            frameLinePaint.setStrokeWidth(dp2px(1));
            frameLinePaint.setStyle(Paint.Style.STROKE);
        }
    }


    public void setCameraManager(@NonNull CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (cameraManager == null) {
            return;
        }

        if (mFrame == null) {
            mFrame = cameraManager.getFramingRect();
            if (mFrame != null) {
                mFrame.offset(0, -mXY[1]);
            }
        }

        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (mFrame == null || previewFrame == null) {
            return;
        }

        drawMaskView(canvas, mFrame);
        drawFrameBounds(canvas, mFrame);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mXY = new int[2];
        getLocationOnScreen(mXY);
    }

    private void drawMaskView(@NonNull Canvas canvas, @NonNull Rect frame) {
        int width = getWidth();
        int height = getHeight();
        mMaskPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mMaskPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mMaskPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mMaskPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mMaskPaint);
    }

    private void drawFrameBounds(@NonNull Canvas canvas, @NonNull Rect frame) {
        if (mFrameLineColor != -1) {
            canvas.drawRect(frame, frameLinePaint);
        }

        int corLength = 32;
        int corWidth = 8;
        //left top
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top + corLength, mCornerPaint);
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth + corLength, frame.top + corWidth, mCornerPaint);
        //right bottom
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength, frame.right, frame.bottom, mCornerPaint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth, frame.right, frame.bottom, mCornerPaint);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
