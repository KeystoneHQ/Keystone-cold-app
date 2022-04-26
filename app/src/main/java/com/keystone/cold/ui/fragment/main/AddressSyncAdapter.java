package com.keystone.cold.ui.fragment.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.AddressSyncItemBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.common.BaseBindingAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AddressSyncAdapter extends BaseBindingAdapter<AddressEntity, AddressSyncItemBinding> {

    private AddressSyncCallback addressSyncCallback;

    private List<Boolean> checkedList;


    public AddressSyncAdapter(Context context) {
        super(context);
    }

    public void setAddressSyncCallback(AddressSyncCallback addressSyncCallback) {
        this.addressSyncCallback = addressSyncCallback;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.address_sync_item;
    }

    @Override
    public void setItems(List<AddressEntity> data) {
        if (checkedList == null || data.size() != checkedList.size()) {
            checkedList = new ArrayList<>();
            for (AddressEntity ignored : data) {
                checkedList.add(false);
            }
        }
        super.setItems(data);
    }

    public boolean toggleChecked(int position) {
        checkedList.set(position, !checkedList.get(position));
        //notifyItemChanged(position);
        return checkedList.get(position);
    }


    public String getDerivationPaths() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < checkedList.size(); i++) {
            if (checkedList.get(i)) {
                jsonArray.put(getItems().get(i).getPath());
            }
        }
        return jsonArray.toString();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        AddressSyncItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
        binding.setAddress(this.items.get(position));
        binding.setPosition(position);
        binding.setCallback(addressSyncCallback);
        binding.setChecked(checkedList.get(position));
    }

    @Override
    protected void onBindItem(AddressSyncItemBinding binding, AddressEntity item) {

    }


}
