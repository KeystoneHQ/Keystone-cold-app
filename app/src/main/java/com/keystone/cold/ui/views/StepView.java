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

public class StepView extends View {

    private final int mTodoPointRadius;
    private final int mDonePointRadius;
    private final int mTodoColor;
    private final int mDoneColor;
    private final int mTodoTextColor;
    private final int mDoneTextColor;
    private final int mLineStrokeWidth;
    private final int mTextMargin;
    private final int mTextSize;
    private final int mPointStart;

    private int mCurrent;
    private int totalStep;

    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mCirclePaint;
    private int mTextHeight;

    String[] mLabels;
    private int mGap;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        mTodoPointRadius = (int) ta.getDimension(R.styleable.StepView_todoPointRadius,
                getResources().getDimension(R.dimen.stepview_todo_point_radius));
        mDonePointRadius = (int) ta.getDimension(R.styleable.StepView_donePointRadius,
                getResources().getDimension(R.dimen.stepview_done_point_radius));
        mTodoColor = ta.getColor(R.styleable.StepView_todoColor,
                getContext().getColor(R.color.stepview_todo_color));
        mDoneColor = ta.getColor(R.styleable.StepView_doneColor,
                getContext().getColor(R.color.stepview_done_color));
        mLineStrokeWidth = (int) ta.getDimension(R.styleable.StepView_lineStrokeWidth,
                getResources().getDimension(R.dimen.stepview_line_stroke_width));
        mTextMargin = (int) ta.getDimension(R.styleable.StepView_textMargin,
                getResources().getDimension(R.dimen.stepview_text_margin));
        mTextSize = (int) ta.getDimension(R.styleable.StepView_textFontSize,
                getResources().getDimension(R.dimen.stepview_text_size));
        mDoneTextColor = ta.getColor(R.styleable.StepView_doneTextColor,
                getContext().getColor(R.color.stepview_done_text_color));
        mTodoTextColor = ta.getColor(R.styleable.StepView_todoTextColor,
                getContext().getColor(R.color.stepview_todo_text_color));
        mPointStart = (int) ta.getDimension(R.styleable.StepView_pointStart,
                getResources().getDimension(R.dimen.stepview_point_margin));

        mCurrent = ta.getInt(R.styleable.StepView_step, 0);
        ta.recycle();
        mLabels = getResources().getStringArray(R.array.step_label);
        setText(mLabels);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int end = getRight() - mPointStart;
        mGap = (end - mPointStart) / (totalStep - 1);
    }

    public void setText(String[] arr) {
        mLabels = arr;
        totalStep = arr.length;
    }

    public void forward() {
        if (mCurrent < totalStep - 1) {
            mCurrent++;
            invalidate();
        }
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mDoneTextColor);
        Rect outRect = new Rect();
        mTextPaint.getTextBounds(mLabels[0], 0, mLabels[0].length(), outRect);
        mTextHeight = outRect.height();
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineStrokeWidth);
        mLinePaint.setColor(getContext().getColor(R.color.colorAccent));

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(getContext().getColor(R.color.colorAccent));
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        drawText(canvas);
        drawPoint(canvas);
    }

    private void drawPoint(Canvas canvas) {
        int x;
        float y = getHeight() / 2f;
        mCirclePaint.setColor(mDoneColor);
        int radius = mDonePointRadius;
        for (int i = 0; i <= totalStep; i++) {
            x = mPointStart + i * mGap;
            if (i > mCurrent) {
                mCirclePaint.setColor(mTodoColor);
                radius = mTodoPointRadius;
            }
            canvas.drawCircle(x, y, radius, mCirclePaint);
        }
    }

    private void drawText(Canvas canvas) {
        mTextPaint.setColor(mDoneTextColor);
        for (int i = 0; i < totalStep; i++) {
            int y = (i & 1) == 0 ? mTextHeight : (int) (getHeight() - mTextPaint.descent());
            if (i > mCurrent) {
                mTextPaint.setColor(mTodoTextColor);
            }
            canvas.drawText(mLabels[i], mPointStart + mGap * i, y, mTextPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        int end = getRight() - mPointStart;
        int mid = mPointStart + mGap * mCurrent;
        mLinePaint.setColor(mDoneColor);
        float y = getHeight() / 2f;
        canvas.drawLine(mPointStart, y, mid, y, mLinePaint);
        mLinePaint.setColor(mTodoColor);
        canvas.drawLine(mid, y, end, y, mLinePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;

        int minHeight = (mTextMargin + mTextHeight) * 2 + mLineStrokeWidth;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = minHeight;
        }
        setMeasuredDimension(widthSize, height);
    }

}