package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemSupportNetworkBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;

public class SupportNetworkAdapter extends BaseBindingAdapter<Integer, ItemSupportNetworkBinding> {

    public SupportNetworkAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_support_network;
    }

    @Override
    protected void onBindItem(ItemSupportNetworkBinding binding, Integer resId) {
        binding.setIconResId(resId);
    }

}