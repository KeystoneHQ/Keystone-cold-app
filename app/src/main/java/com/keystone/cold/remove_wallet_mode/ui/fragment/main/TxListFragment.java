/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentTxListBinding;
import com.keystone.cold.databinding.ItemTxListBinding;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.remove_wallet_mode.helper.ReviewDestinationDetector;
import com.keystone.cold.remove_wallet_mode.viewmodel.record.TxRecordViewModel;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.TxCallback;

import java.util.List;


public class TxListFragment extends BaseFragment<FragmentTxListBinding> {

    private TxRecordViewModel viewModel;
    private LiveData<List<Tx>> txLiveData;
    private TxAdapter adapter;

    private final TxCallback txCallback = tx -> {
        Destination destination = ReviewDestinationDetector.detect(tx);
        if (destination != null) {
            navigate(destination.id, destination.bundle);
        }
    };

    static Fragment newInstance(@NonNull String coinId, @NonNull String coinCode) {
        TxListFragment fragment = new TxListFragment();
        Bundle args = new Bundle();
        args.putString(BundleKeys.COIN_ID_KEY, coinId);
        args.putString(BundleKeys.COIN_CODE_KEY, coinCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_tx_list;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(this).get(TxRecordViewModel.class);
        if (adapter == null) {
            adapter = new TxAdapter(mActivity);
        }
        mBinding.list.setAdapter(adapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle bundle = requireArguments();
        String coinId = bundle.getString(BundleKeys.COIN_ID_KEY);
        String canonicalCoinId = coinId;
        if (Coins.BTC.coinId().equals(coinId)) {
            BTCAccount btcAccount = BTCAccount.ofCode(Utilities.getCurrentBTCAccount(mActivity));
            canonicalCoinId = btcAccount.getCoinId();
        }
        txLiveData = viewModel.loadTxs(canonicalCoinId);
        subscribeUI(txLiveData);
    }

    private void subscribeUI(LiveData<List<Tx>> txLiveData) {
        txLiveData.observe(this, txEntities -> {
            adapter.setItems(txEntities);
        });
    }


    @Override
    public void onDestroyView() {
        if (txLiveData != null) {
            txLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    class TxAdapter extends FilterableBaseBindingAdapter<Tx, ItemTxListBinding> {

        TxAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_tx_list;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
        }

        @Override
        protected void onBindItem(ItemTxListBinding binding, Tx item) {
            binding.setTx(item);
            binding.setTxCallback(txCallback);
        }

        @Override
        public void setItems(List<Tx> data) {
            allItems.clear();
            allItems.addAll(data);
            super.setItems(data);
        }
    }

}


