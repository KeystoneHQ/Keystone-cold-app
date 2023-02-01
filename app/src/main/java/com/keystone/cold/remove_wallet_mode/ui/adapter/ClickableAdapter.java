package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemClickableBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.ClickableItem;
import com.keystone.cold.ui.common.BaseBindingAdapter;

public class ClickableAdapter extends BaseBindingAdapter<ClickableItem, ItemClickableBinding> {
    private ClickableCallback callback;
    public ClickableAdapter(Context context, ClickableCallback callback) {
        super(context);
        this.callback = callback;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_clickable;
    }

    @Override
    protected void onBindItem(ItemClickableBinding binding, ClickableItem item) {
        binding.setCallback(callback);
        binding.setClickableItem(item);
    }

    public interface ClickableCallback {
        void onClick(ClickableItem item);
    }
}
