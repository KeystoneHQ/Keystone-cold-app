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
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class SpanedTextView extends AppCompatTextView {

    private CharSequence origin;
    private OnClickListener[] clickListeners;

    public SpanedTextView(Context context) {
        super(context);
    }

    public SpanedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpanedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setClickListeners(OnClickListener[] clickListeners) {
        this.clickListeners = clickListeners;
        setText(origin, BufferType.SPANNABLE);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (origin == null && !TextUtils.isEmpty(text)) {
            origin = text;
        }
        SpannableStringBuilder spanned = (SpannableStringBuilder) Html.fromHtml(text.toString(), FROM_HTML_MODE_LEGACY);
        UnderlineSpan[] spans = spanned.getSpans(0, text.length(), UnderlineSpan.class);

        int index = 0;
        for (UnderlineSpan underline : spans) {
            int start = spanned.getSpanStart(underline);
            int end = spanned.getSpanEnd(underline);
            if (clickListeners != null && clickListeners.length > index) {
                spanned.setSpan(new InternalClickableSpan(clickListeners[index++]),
                        start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            spanned.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        setMovementMethod(LinkMovementMethod.getInstance());
        super.setText(spanned, type);

    }


}

class InternalClickableSpan extends ClickableSpan {

    private final OnClickListener listener;

    InternalClickableSpan(OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = Color.BLACK;
    }


    @Override
    public void onClick(@NonNull View widget) {
        if (listener != null) {
            listener.onClick(widget);
        }
    }
}