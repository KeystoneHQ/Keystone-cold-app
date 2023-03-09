package com.keystone.cold.remove_wallet_mode.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemIndexAddressPairBinding;
import com.keystone.cold.databinding.ItemPathPatternViewBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;
import com.keystone.cold.ui.common.BaseBindingAdapter;


public class PathPatternItemView extends LinearLayout {

    private final ItemPathPatternViewBinding binding;

    public PathPatternItemView(Context context) {
        this(context, null);
    }

    public PathPatternItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathPatternItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PathPatternItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.item_path_pattern_view, null, false);
        addView(binding.getRoot());
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setData(PathPatternItem pathPatternItemModel) {
        binding.setPathPattern(pathPatternItemModel.getPathPattern());
        binding.setPatternName(pathPatternItemModel.getPatternName());
        binding.setIsShowName(TextUtils.isEmpty(pathPatternItemModel.getPatternName()));
        binding.setIsRecommend(pathPatternItemModel.isRecommend());
        binding.setIsSelected(pathPatternItemModel.getIsSelect());
        binding.setDescription(pathPatternItemModel.getDescription());
        AccountAdapter accountAdapter = new AccountAdapter(getContext());
        accountAdapter.setItems(pathPatternItemModel.getPairs());
        binding.addressList.setAdapter(accountAdapter);
        binding.addressList.setOnTouchListener(this::onTouch);
    }

    private boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return false;
    }


    static class AccountAdapter extends BaseBindingAdapter<Pair<String, String>, ItemIndexAddressPairBinding> {
        AccountAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_index_address_pair;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemIndexAddressPairBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding == null) return;
            binding.key.setText(items.get(position).first);
            binding.value.setText(items.get(position).second);
        }

        @Override
        protected void onBindItem(ItemIndexAddressPairBinding binding, Pair<String, String> item) {
        }
    }
}
