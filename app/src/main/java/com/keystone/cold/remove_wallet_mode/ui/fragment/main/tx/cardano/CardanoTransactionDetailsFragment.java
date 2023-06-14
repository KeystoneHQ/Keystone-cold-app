package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentCardanoDetailBinding;
import com.keystone.cold.databinding.ItemAddressSeletedListBinding;
import com.keystone.cold.databinding.ItemCardanoAddressBinding;
import com.keystone.cold.databinding.ItemCardanoStakeActionBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;

public class CardanoTransactionDetailsFragment extends BaseFragment<FragmentCardanoDetailBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_cardano_detail;
    }

    private final MutableLiveData<CardanoTransaction> transaction;

    public CardanoTransactionDetailsFragment(MutableLiveData<CardanoTransaction> transaction) {
        this.transaction = transaction;
    }

    public static CardanoTransactionDetailsFragment newInstance(Bundle bundle, MutableLiveData<CardanoTransaction> transaction) {
        CardanoTransactionDetailsFragment fragment = new CardanoTransactionDetailsFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void init(View view) {
        transaction.observe(this, v -> {
            if (v == null) return;
            mBinding.setTx(v.getDetail());
            mBinding.setCoinCode(Coins.ADA.coinCode());
            mBinding.setCheckInfoTitle(Coins.ADA.coinName());
            StakeActionAdapter actionAdapter = new StakeActionAdapter(mActivity);
            mBinding.actionList.setAdapter(actionAdapter);
            actionAdapter.setItems(v.getDetail().getActions());

            CardanoAddressAdapter fromAdapter = new CardanoAddressAdapter(mActivity);
            fromAdapter.setItems(v.getDetail().getFrom());
            mBinding.fromList.setLayoutManager(new LinearLayoutManager(mActivity) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            mBinding.fromList.setAdapter(fromAdapter);

            LinearLayoutManager layoutManager2 = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            CardanoAddressAdapter toAdapter = new CardanoAddressAdapter(mActivity);
            toAdapter.setItems(v.getDetail().getTo());
            mBinding.toList.setAdapter(toAdapter);
            mBinding.toList.setLayoutManager(layoutManager2);

        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private static class StakeActionAdapter extends BaseBindingAdapter<CardanoTransaction.CardanoStakeAction, ItemCardanoStakeActionBinding> {
        public StakeActionAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_cardano_stake_action;
        }

        @Override
        protected void onBindItem(ItemCardanoStakeActionBinding binding, CardanoTransaction.CardanoStakeAction item) {
            binding.setAction(item);
        }
    }

    private static class CardanoAddressAdapter extends BaseBindingAdapter<CardanoTransaction.CardanoAddress, ItemCardanoAddressBinding> {

        public CardanoAddressAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_cardano_address;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemCardanoAddressBinding binding = DataBindingUtil.getBinding(holder.itemView);
            assert binding != null;
            binding.setAddress(items.get(position));
            binding.setIndex("" + position);
        }

        @Override
        protected void onBindItem(ItemCardanoAddressBinding binding, CardanoTransaction.CardanoAddress item) {
//            binding.setAddress(item);
        }
    }
}
