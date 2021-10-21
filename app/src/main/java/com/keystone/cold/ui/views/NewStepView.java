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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.keystone.cold.R;

public class NewStepView extends View {

    private final int mTitleColor;
    private final int mDoneTextColor;
    private final int mTextMargin;
    private final int mTextSize;
    private final int mTextSmallSize;
    private final int mPointStart;


    private final int mCurrent;

    private Paint mTextPaint;
    private int mTextHeight;

    private Paint mStepPaint;

    String[] mLabels;

    public NewStepView(Context context) {
        this(context, null);
    }

    public NewStepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewStepView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NewStepView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        mTitleColor = getContext().getColor(R.color.white);
        mTextMargin = (int) ta.getDimension(R.styleable.StepView_textMargin,
                getResources().getDimension(R.dimen.newstepview_text_margin));
        mTextSize = (int) ta.getDimension(R.styleable.StepView_textFontSize,
                getResources().getDimension(R.dimen.newstepview_text_size));
        mTextSmallSize = (int) ta.getDimension(R.styleable.StepView_textFontSizeSmall,
                getResources().getDimension(R.dimen.newstepview_text_small_size));
        mDoneTextColor = ta.getColor(R.styleable.StepView_doneTextColor,
                getContext().getColor(R.color.stepview_done_color));
        mPointStart = (int) ta.getDimension(R.styleable.StepView_pointStart,
                getResources().getDimension(R.dimen.newstepview_point_margin));

        mCurrent = ta.getInt(R.styleable.StepView_step, 0);
        ta.recycle();
        mLabels = getResources().getStringArray(R.array.step_label);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        Rect outRect = new Rect();
        mTextPaint.getTextBounds(mLabels[mCurrent], 0, mLabels[mCurrent].length(), outRect);
        mTextHeight = outRect.height();
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mStepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStepPaint.setTextSize(mTextSmallSize);
        mStepPaint.setColor(mDoneTextColor);
        mStepPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        float titleWidth = mTextPaint.measureText(mLabels[mCurrent]);
        float stepPart1 = mTextPaint.measureText("5");
        float stepPart2 = mStepPaint.measureText("/5");
        float space = canvas.getWidth() - 2 * mPointStart - titleWidth - stepPart1 - stepPart2;

        float x0 = mPointStart;
        float x1 = x0 + titleWidth + space;
        float x2 = x1 + stepPart1;

        mTextPaint.setColor(mTitleColor);
        canvas.drawText(mLabels[mCurrent], x0, mTextHeight + mTextMargin, mTextPaint);

        mTextPaint.setColor(mDoneTextColor);
        canvas.drawText(String.valueOf(mCurrent+1), x1, mTextHeight + mTextMargin, mTextPaint);

        mStepPaint.setColor(mTitleColor);
        canvas.drawText("/"+ mLabels.length, x2, mTextHeight + mTextMargin, mStepPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;

        int minHeight = mTextMargin * 2 + mTextHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = minHeight;
        }
        setMeasuredDimension(widthSize, height);
    }

}