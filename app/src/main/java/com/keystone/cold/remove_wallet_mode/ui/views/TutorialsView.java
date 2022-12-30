package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemTutorialsBinding;
import com.keystone.cold.ui.fragment.main.tutorials.TutorialItem;

import java.util.List;

public class TutorialsView extends LinearLayout {

    public TutorialsView(Context context) {
        this(context, null);
    }

    public TutorialsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TutorialsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TutorialsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setItemData(List<TutorialItem> tutorialItems) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (TutorialItem tutorialItem : tutorialItems) {
            ItemTutorialsBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_tutorials, null, false);
            binding.name.setText(tutorialItem.getName());
            binding.link.setText(tutorialItem.getLink());
            binding.link.setAutoLinkMask(Linkify.WEB_URLS);
            binding.getRoot().setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClickItem(tutorialItem);
                }
            });
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
            addView(binding.getRoot(), layoutParams);

        }
    }

    public interface OnItemClickListener {
        void onClickItem(TutorialItem tutorialItem);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
