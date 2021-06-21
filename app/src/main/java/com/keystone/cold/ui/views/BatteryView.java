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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.R;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.util.Locale;


public class BatteryView extends View implements OnBatteryChangeListener {
    @Nullable
    private static Bitmap sEmptyBatteryBitmap;
    private final Paint mPaint;
    private final float mPowerAreaWidth;
    private final float mPowerAreaHeight;
    private final float mMarginLeftTop;
    private final float mBorderRadius;
    private int mPercentage = 50;

    private BroadcastReceiver mReceiver;
    private TextView mPercentText;

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = initPaint();

        final Resources resources = context.getResources();
        mBorderRadius = resources.getDimension(R.dimen.battery_power_border_radius);
        mMarginLeftTop = resources.getDimension(R.dimen.battery_power_margin_left_top);
        mPowerAreaWidth = resources.getDimension(R.dimen.battery_power_width);
        mPowerAreaHeight = resources.getDimension(R.dimen.battery_power_height);
    }

    private Paint initPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (sEmptyBatteryBitmap == null) {
            sEmptyBatteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.battery_empty);
        }

        setMeasuredDimension(sEmptyBatteryBitmap.getWidth(), sEmptyBatteryBitmap.getHeight());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mReceiver = registerBroadcastReceiver(getContext());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterBroadcastReceiver(getContext(), mReceiver);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sEmptyBatteryBitmap == null) {
            return;
        }

        canvas.drawBitmap(sEmptyBatteryBitmap, 0, 0, mPaint);

        final float currentWidth = mPowerAreaWidth * mPercentage / 100F;
        canvas.drawRoundRect(mMarginLeftTop, mMarginLeftTop,
                mMarginLeftTop + currentWidth,
                mMarginLeftTop + mPowerAreaHeight,
                mBorderRadius, mBorderRadius, mPaint);
    }

    @NonNull
    private BroadcastReceiver registerBroadcastReceiver(@NonNull Context context) {
        Preconditions.checkNotNull(context);
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final BroadcastReceiver receiver = new BatteryReceiver(this);
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    private void unregisterBroadcastReceiver(@NonNull Context context, @Nullable BroadcastReceiver receiver) {
        Preconditions.checkNotNull(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBatteryChange(int percentage) {
        mPercentage = percentage;
        if (mPercentText == null) {
            mPercentText = ((ViewGroup) getParent()).findViewById(R.id.percentage_text);
        }
        if (mPercentText != null) {
            mPercentText.setText(String.format(Locale.US, "%d%%", mPercentage));
        }
        invalidate();
    }


    public static class BatteryReceiver extends BroadcastReceiver {
        private final OnBatteryChangeListener mListener;

        BatteryReceiver(@NonNull OnBatteryChangeListener listener) {
            mListener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBatteryStatus(intent);
        }

        private void onReceiveBatteryStatus(@Nullable Intent intent) {
            if (intent == null) {
                return;
            }

            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            final int percentage = Math.max(0, Math.min(100, (int) (level / (float) scale * 100)));
            mListener.onBatteryChange(percentage);
        }
    }
}

interface OnBatteryChangeListener {
    void onBatteryChange(@IntRange(from = 0, to = 100) int percentage);
}
