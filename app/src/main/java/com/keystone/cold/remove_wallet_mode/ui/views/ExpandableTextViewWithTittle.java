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


public class ExpandableTextViewWithTittle extends LinearLayout {

    private TextView tvTittle;
    private TextView tvContent;
    private ImageView imSwitcher;

    private final MutableLiveData<Boolean> isExpanded = new MutableLiveData<>(null);
    private int collapseLines;

    private final Observer<Boolean> expandedWatcher = (v) -> {
        if (v == null) return;
        if (tvContent == null) return;
        if (imSwitcher == null) return;
        if (v) {
            tvContent.setMaxLines(Integer.MAX_VALUE);
            imSwitcher.setImageResource(R.drawable.icon_fold);
        } else {
            tvContent.setMaxLines(collapseLines);
            imSwitcher.setImageResource(R.drawable.icon_unfold);
        }
        invalidate();
    };

    public ExpandableTextViewWithTittle(Context context) {
        this(context, null);
    }

    public ExpandableTextViewWithTittle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextViewWithTittle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExpandableTextViewWithTittle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_expandable_textview_with_tittle, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextViewWithTittle);
        try {
            boolean originExpanded = typedArray.getBoolean(R.styleable.ExpandableTextViewWithTittle_isExpanded, false);
            collapseLines = typedArray.getInt(R.styleable.ExpandableTextViewWithTittle_collapsedLines, 0);
            CharSequence tittle = typedArray.getText(R.styleable.ExpandableTextViewWithTittle_tittle);
            CharSequence content = typedArray.getText(R.styleable.ExpandableTextViewWithTittle_content);

            tvTittle = findViewById(R.id.tvTittle);
            imSwitcher = findViewById(R.id.imSwitch);
            tvContent = findViewById(R.id.tvContent);

            tvTittle.setText(tittle);
            tvContent.setText(content);

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
        }
    }

    public void setTittle(String tittle) {
        if (tvTittle != null) {
            tvTittle.setText(tittle);
        }
    }

    public void setContent(String content) {
        if (tvContent != null) {
            tvContent.setText(content);
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
