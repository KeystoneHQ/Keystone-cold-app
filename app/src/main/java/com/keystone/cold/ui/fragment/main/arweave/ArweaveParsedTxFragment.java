package com.keystone.cold.ui.fragment.main.arweave;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveItemTagBinding;
import com.keystone.cold.databinding.ArweaveParsedTxBinding;
import com.keystone.cold.databinding.TxDetailItemBinding;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;

public class ArweaveParsedTxFragment extends BaseFragment<ArweaveParsedTxBinding> {

    private ArweaveTransaction tx;

    @Override
    protected int setView() {
        return R.layout.arweave_parsed_tx;
    }

    public ArweaveParsedTxFragment(ArweaveTransaction tx) {
        this.tx = tx;
    }

    @Override
    protected void init(View view) {
        mBinding.setTx(tx);
        TagAdapter tagAdapter = new TagAdapter(mActivity);
        if (tx.getTags().size() == 0) {
            mBinding.tags.setVisibility(View.GONE);
        } else {
            mBinding.tagsList.setAdapter(tagAdapter);
            tagAdapter.setItems(tx.getTags());
        }
    }

    public void setTx(ArweaveTransaction tx) {
        this.tx = tx;
    }

    public static ArweaveParsedTxFragment newInstance(ArweaveTransaction tx) {
        return new ArweaveParsedTxFragment(tx);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    static class TagAdapter extends BaseBindingAdapter<ArweaveTransaction.Tag, ArweaveItemTagBinding> {

        public TagAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.arweave_item_tag;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ArweaveItemTagBinding binding = DataBindingUtil.getBinding(holder.itemView);
            onBindItem(binding, this.items.get(position));
        }

        @Override
        protected void onBindItem(ArweaveItemTagBinding binding, ArweaveTransaction.Tag item) {
            binding.setTag(item);
        }
    }
}
