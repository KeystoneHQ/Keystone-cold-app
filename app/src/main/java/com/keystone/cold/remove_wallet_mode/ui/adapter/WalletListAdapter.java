package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemWalletListBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;

public class WalletListAdapter extends FilterableBaseBindingAdapter<WalletItem, ItemWalletListBinding>{

    private final Context context;
    private final ClickCallback clickCallback;


    public WalletListAdapter(Context context, @Nullable ClickCallback clickCallback) {
        super(context);
        this.context = context;
        this.clickCallback = clickCallback;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_wallet_list;
    }

    @Override
    protected void onBindItem(ItemWalletListBinding binding, WalletItem item) {
        binding.setWalletItem(item);
        binding.setCallback(clickCallback);
    }

    public interface ClickCallback {
        void onClick(WalletItem walletItem);
    }

}

