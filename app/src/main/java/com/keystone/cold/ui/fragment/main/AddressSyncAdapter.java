package com.keystone.cold.ui.fragment.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AddressSyncItemBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        if(WatchWallet.getWatchWallet(context).equals(WatchWallet.POLKADOT_JS)) {
            boolean current = checkedList.get(position);
            resetCheckStatus();
            checkedList.set(position, !current);
            for (int i = 0; i < checkedList.size(); i++) {
                notifyItemChanged(i);
            }
        }
        else {
            checkedList.set(position, !checkedList.get(position));
            notifyItemChanged(position);
        }
        return checkedList.get(position);
    }

    public void resetCheckStatus() {
        for (int i = 0; i < checkedList.size(); i++) {
            checkedList.set(i, false);
        }
    }

    public boolean existSelectedAddress(){
       return checkedList.contains(true);
    }

    public List<SyncInfo> getSyncInfo() {
        List<SyncInfo> syncInfoList = new ArrayList<>();
        for (int i = 0; i < checkedList.size(); i++) {
            if (checkedList.get(i)) {
                SyncInfo syncInfo = new SyncInfo();
                syncInfo.setCoinId(getItems().get(i).getCoinId());
                syncInfo.setAddress(getItems().get(i).getAddressString());
                syncInfo.setPath(getItems().get(i).getPath());
                syncInfo.setName(getItems().get(i).getName());
                syncInfo.setAddition(getItems().get(i).getAddition());
                syncInfoList.add(syncInfo);
            }
        }
        return syncInfoList;
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
