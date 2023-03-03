package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.keystone.cold.R;

public class FaqListView extends LinearLayout {
    public FaqListView(Context context) {
        this(context, null);
    }

    public FaqListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaqListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FaqListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setData(String[] tittle, String[] content) {
        if (tittle.length != content.length) return;
        for (int i = 0; i < tittle.length; i++) {
            ExpandableTextViewWithTittle expandableTextViewWithTittle = new ExpandableTextViewWithTittle(getContext());
            expandableTextViewWithTittle.setTittle(tittle[i]);
            expandableTextViewWithTittle.setContent(content[i]);
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = (int) getResources().getDimension(R.dimen.faq_item_margin);
            addView(expandableTextViewWithTittle, layoutParams);
        }
    }

}
