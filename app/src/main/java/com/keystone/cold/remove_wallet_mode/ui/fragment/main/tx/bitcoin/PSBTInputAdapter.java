package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin;

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

class PSBTInputAdapter extends BaseBindingAdapter<JSONObject, TxDetailItemBinding> {
    private int position;

    public PSBTInputAdapter(Context context) {
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
            binding.label.setText("Input" + " " + this.position);
            String value = item.getString("value");
            String address = item.optString("address");
            String pubkey = item.getString("pubkey");
            boolean isMine = item.getBoolean("is_mine");
            if (isMine) {
                binding.mine.setVisibility(View.VISIBLE);
                binding.info.setText(value + "\n" + address);
            } else {
                binding.info.setText(value + "\n" + "Public Key: " + pubkey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
