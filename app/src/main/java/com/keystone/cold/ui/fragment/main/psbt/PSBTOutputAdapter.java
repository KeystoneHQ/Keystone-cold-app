package com.keystone.cold.ui.fragment.main.psbt;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.TxDetailItemBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;

import org.json.JSONException;
import org.json.JSONObject;

class PSBTOutputAdapter extends BaseBindingAdapter<JSONObject, TxDetailItemBinding> {
    private int position;

    public PSBTOutputAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.tx_detail_item;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TxDetailItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
        this.position = holder.getAdapterPosition();
        onBindItem(binding, this.items.get(position));
    }

    @Override
    protected void onBindItem(TxDetailItemBinding binding, JSONObject item) {
        try {
            binding.label.setText("Output" + " " + this.position);
            boolean isChange = item.getBoolean("is_change");
            String value = item.getString("value");
            String address = item.getString("address");
            String path = item.optString("change_path");
            if (isChange) {
                binding.info.setText(value + "\n" + address + "\n" + path);
                binding.change.setVisibility(View.VISIBLE);
            } else {
                binding.info.setText(value + "\n" + address);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
