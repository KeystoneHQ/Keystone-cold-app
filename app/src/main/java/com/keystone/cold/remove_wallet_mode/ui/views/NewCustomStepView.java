package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.StepViewBinding;

public class NewCustomStepView extends LinearLayout {

    private LayoutInflater inflater;
    String[] mLabels;
    private final int mCurrent;


    public NewCustomStepView(Context context) {
        this(context, null);
    }

    public NewCustomStepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewCustomStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NewCustomStepView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        mCurrent = ta.getInt(R.styleable.StepView_step, 0);
        mLabels = getResources().getStringArray(R.array.step_label_remove_wallet_mode);
        ta.recycle();
        init();
    }

    private void init() {
        inflater = LayoutInflater.from(getContext());
        StepViewBinding stepViewBinding = DataBindingUtil.inflate(inflater, R.layout.step_view, null, false);
        stepViewBinding.tvTittle.setText(mLabels[mCurrent]);
        stepViewBinding.tvDone.setText("" + (mCurrent+1));
        stepViewBinding.tvAll.setText("/" + mLabels.length);
        addView(stepViewBinding.getRoot());
    }


}
