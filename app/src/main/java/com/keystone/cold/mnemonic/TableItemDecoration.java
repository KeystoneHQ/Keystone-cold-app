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

package com.keystone.cold.mnemonic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;


class TableItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private final int dividerWidth;

    public TableItemDecoration(Context context) {
        mPaint = new Paint();
        mPaint.setColor(context.getColor(R.color.white40));
        dividerWidth = dp2px(context, 1);
        mPaint.setStrokeWidth(dividerWidth);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    private int getSpanCount(RecyclerView parent) {

        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final int left = child.getLeft();
            final int right = child.getRight();
            final int top = child.getBottom();

            if (i + getSpanCount(parent) >= childCount) {
                c.drawLine(left, top - dividerWidth, right, top - dividerWidth, mPaint);
            } else {
                c.drawLine(left, top, right, top, mPaint);
            }

            //first row
            if (i < getSpanCount(parent)) {
                c.drawLine(left, child.getTop() + dividerWidth / 2, right,
                        child.getTop() + dividerWidth / 2, mPaint);
            }
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final int top = child.getTop();
            final int bottom = child.getBottom();
            final int left = child.getRight();

            //last column
            if ((i + 1) % getSpanCount(parent) == 0) {
                c.drawLine(left - dividerWidth, top,
                        left - dividerWidth, bottom, mPaint);
            } else {
                c.drawLine(left, top, left, bottom, mPaint);
            }

            //first column
            if (i % getSpanCount(parent) == 0) {
                c.drawLine(child.getLeft() + dividerWidth / 2, top,
                        child.getLeft() + dividerWidth / 2, bottom, mPaint);
            }
        }
    }


    private int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }
}
