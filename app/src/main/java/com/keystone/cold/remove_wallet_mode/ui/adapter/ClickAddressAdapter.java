package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemAddressClickableBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.ui.common.BaseBindingAdapter;

public class ClickAddressAdapter extends BaseBindingAdapter<AddressItem, ItemAddressClickableBinding> {
    private final AddressCallback addressCallback;

    public ClickAddressAdapter(Context context, AddressCallback addressCallback) {
        super(context);
        this.addressCallback = addressCallback;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_address_clickable;
    }

    @Override
    protected void onBindItem(ItemAddressClickableBinding binding, AddressItem item) {
        binding.setAddressItem(item);
        binding.setCallback(addressCallback);
    }

    public interface AddressCallback {
        void onClick(AddressItem addr);
    }
}
