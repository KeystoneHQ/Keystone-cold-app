package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveItemTagBinding;
import com.keystone.cold.databinding.FragmentArweaveTxBinding;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;

public class ARweaveTransactionDetailFragment extends BaseFragment<FragmentArweaveTxBinding> {
    private final MutableLiveData<ArweaveTransaction> transaction;

    public ARweaveTransactionDetailFragment(MutableLiveData<ArweaveTransaction> transaction) {
        this.transaction = transaction;
    }

    public static ARweaveTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<ArweaveTransaction> transaction) {
        ARweaveTransactionDetailFragment fragment = new ARweaveTransactionDetailFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_arweave_tx;
    }

    @Override
    protected void init(View view) {
        transaction.observe(this, this::updateUI);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void updateUI(ArweaveTransaction transaction) {
        if (transaction == null) return;
        mBinding.setTx(transaction);
        mBinding.setCoinCode(Coins.AR.coinCode());
        mBinding.setCheckInfoTitle(Coins.AR.coinName());
        TagAdapter tagAdapter = new TagAdapter(mActivity);
        if (transaction.getTags().size() == 0) {
            mBinding.tags.setVisibility(View.GONE);
        } else {
            mBinding.tagsList.setAdapter(tagAdapter);
            tagAdapter.setItems(transaction.getTags());
        }
        if (transaction.getSignatureUR() != null) {
            mBinding.qrcodeContainer.setVisibility(View.VISIBLE);
            mBinding.qrcode.qrcode.setData(transaction.getSignatureUR());
        }
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
