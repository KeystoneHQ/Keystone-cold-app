package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.AddressSyncItemBinding;
import com.keystone.cold.databinding.ItemAddressSeletedListBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.ui.common.BaseBindingAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectedAddressAdapter extends BaseBindingAdapter<AddressItem, ItemAddressSeletedListBinding> {


    private List<Boolean> checkedList;
    private AddressCallback addressCallback;

    public SelectedAddressAdapter(Context context, AddressCallback addressCallback) {
        super(context);
        this.addressCallback = addressCallback;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_address_seleted_list;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemAddressSeletedListBinding binding = DataBindingUtil.getBinding(holder.itemView);
        assert binding != null;
        binding.setPosition(position);
        binding.setCallback(addressCallback);
        binding.setSelected(checkedList.get(position));
        binding.setAddressItem(items.get(position));
    }

    @Override
    protected void onBindItem(ItemAddressSeletedListBinding binding, AddressItem item) {
    }


    @Override
    public void setItems(List<AddressItem> data) {

        if (checkedList == null) {
            checkedList = new ArrayList<>();
            for (AddressItem ignored : data) {
                checkedList.add(false);
            }
        } else if (data.size() > checkedList.size()) {
            int del = data.size() - checkedList.size();
            for (int i = 0; i < del; i++) {
                checkedList.add(false);
            }
        }
//        if (checkedList == null || data.size() != checkedList.size()) {
//            checkedList = new ArrayList<>();
//            for (AddressItem ignored : data) {
//                checkedList.add(false);
//            }
//        }
        super.setItems(data);
    }


    public void toggleChecked(int position) {
        checkedList.set(position, !checkedList.get(position));
        notifyItemChanged(position);
    }

    public boolean existSelectedAddress() {
        return checkedList.contains(true);
    }

    public interface AddressCallback {
        void onClick(AddressItem addr, int position);
    }
}
