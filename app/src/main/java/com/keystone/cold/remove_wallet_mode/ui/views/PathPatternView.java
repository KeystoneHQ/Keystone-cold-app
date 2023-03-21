package com.keystone.cold.remove_wallet_mode.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;

import java.util.List;


public class PathPatternView extends LinearLayout {

    public PathPatternView(Context context) {
        this(context, null);
    }

    public PathPatternView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathPatternView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PathPatternView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private List<PathPatternItem> pathPatternItems;

    public void setData(final List<PathPatternItem> pathPatternItems) {
        removeAllViews();
        this.pathPatternItems = pathPatternItems;
        for (int i = 0; i < pathPatternItems.size(); i++) {
            PathPatternItemView pathPatternItemView = new PathPatternItemView(getContext());
            PathPatternItem pathPatternItem = pathPatternItems.get(i);
            pathPatternItemView.setData(pathPatternItem);
            int index = i;
            pathPatternItemView.setOnClickListener(v -> onClick(index, pathPatternItems));
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
            addView(pathPatternItemView, layoutParams);
        }
    }

    private void onClick(int index, List<PathPatternItem> pathPatternItems) {
        for (int i = 0; i < pathPatternItems.size(); i++) {
            pathPatternItems.get(i).getIsSelect().set(i == index);
        }
        if (onItemClick != null) onItemClick.onItemClick(pathPatternItems.get(index).getCode());
    }

    public void uncheckAll() {
        if (pathPatternItems != null) {
            for (int i = 0; i < pathPatternItems.size(); i++) {
                pathPatternItems.get(i).getIsSelect().set(false);
            }
        }
    }


    public interface OnItemClick {
        void onItemClick(String code);
    }

    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

}
