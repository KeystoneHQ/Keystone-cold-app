package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.keystone.cold.R;


public class ExpandableTextView extends LinearLayout {

    private TextView textView;
    private ImageView imageView;

    private final MutableLiveData<Boolean> isExpanded = new MutableLiveData<>(null);
    private int collapseLines;

    private final Observer<Boolean> expandedWatcher = (v) -> {
        if (v == null) return;
        if (textView == null) return;
        if (imageView == null) return;
        if (v) {
            textView.setMaxLines(Integer.MAX_VALUE);
            imageView.setImageResource(R.drawable.icon_fold);
        } else {
            textView.setMaxLines(collapseLines);
            imageView.setImageResource(R.drawable.icon_unfold);
        }
        invalidate();
    };

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextView getTextView() {
        return textView;
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.view_expandable_textview, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        try {
            boolean originExpanded = typedArray.getBoolean(R.styleable.ExpandableTextView_isExpanded, false);
            collapseLines = typedArray.getInt(R.styleable.ExpandableTextView_collapsedLines, 1);
            CharSequence text = typedArray.getText(R.styleable.ExpandableTextView_text);
            textView = (TextView) getChildAt(0);
            imageView = (ImageView) getChildAt(1);
            textView.setText(text);
            if (collapseLines < 1) {
                collapseLines = 1;
            }
            isExpanded.postValue(originExpanded);
            this.setOnClickListener((v) -> {
                switchView();
            });
        } finally {
            typedArray.recycle();
        }
        invalidate();
    }

    private void switchView() {
        Boolean origin = isExpanded.getValue();
        if (origin != null) {
            isExpanded.postValue(!origin);
            if (!origin) {
                imageView.setImageResource(R.drawable.icon_fold);
            }else {
                imageView.setImageResource(R.drawable.icon_unfold);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isExpanded.observeForever(expandedWatcher);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isExpanded.removeObserver(expandedWatcher);
    }
}
