package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.TagAssetEcologyBinding;

import java.util.List;

public class EcologyTagView extends LinearLayout {

    private LayoutInflater inflater;


    public EcologyTagView(Context context) {
        this(context, null);
    }

    public EcologyTagView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EcologyTagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EcologyTagView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(getContext());
    }

    public void setData(List<String> ecologyTags) {
        removeAllViews();
        if (ecologyTags == null || ecologyTags.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        for (String tag: ecologyTags) {
            TagAssetEcologyBinding binding = DataBindingUtil.inflate(inflater, R.layout.tag_asset_ecology, null, false);
            binding.tvEcology.setText(tag);
            addView(binding.getRoot(), getStartMargin());
        }
        setVisibility(VISIBLE);
    }

    private LayoutParams getStartMargin() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.dp_16);
        return layoutParams;
    }
}
