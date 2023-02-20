package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemCheckableBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.CheckableItem;
import com.keystone.cold.ui.common.BaseBindingAdapter;

import java.util.ArrayList;
import java.util.List;

public class CheckableAdapter extends BaseBindingAdapter<CheckableItem, ItemCheckableBinding> {
    private final CheckableCallback callback;
    private final List<CheckableItem> checkedList = new ArrayList<>();

    public CheckableAdapter(Context context, CheckableCallback onChecked) {
        super(context);
        this.callback = item -> {
            if (checkedList.contains(item)) {
                item.setChecked(false);
                checkedList.remove(item);
            } else {
                item.setChecked(true);
                checkedList.add(item);
                if (onChecked != null) {
                    onChecked.onClick(item);
                }
            }
        };
    }

    public List<CheckableItem> getCheckedList() {
        return checkedList;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_clickable;
    }

    @Override
    protected void onBindItem(ItemCheckableBinding binding, CheckableItem item) {
        binding.setCallback(callback);
        binding.setCheckableItem(item);
    }

    public interface CheckableCallback {
        void onClick(CheckableItem item);
    }
}
