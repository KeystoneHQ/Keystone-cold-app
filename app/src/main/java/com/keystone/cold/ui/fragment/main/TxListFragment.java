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

package com.keystone.cold.ui.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.TxListBinding;
import com.keystone.cold.databinding.TxListItemBinding;
import com.keystone.cold.db.entity.GenericETHTxEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.model.Tx;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;

import org.json.JSONException;
import org.json.JSONObject;

public class TxListFragment extends BaseFragment<TxListBinding> {

    private FilterableBaseBindingAdapter adapter;
    private TxCallback txCallback;
    private String query;
    private Comparator<Tx> txEntityComparator;
    private String coinCode;
    private WatchWallet watchWallet;


    static Fragment newInstance(@NonNull String coinId, @NonNull String coinCode) {
        TxListFragment fragment = new TxListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_COIN_ID, coinId);
        args.putString(KEY_COIN_CODE, coinCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.tx_list;
    }

    @Override
    protected void init(View view) {
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(CoinListViewModel.class);
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        adapter = new TxAdapter(mActivity);
        mBinding.list.setAdapter(adapter);
        if (watchWallet == WatchWallet.METAMASK) {
            viewModel.loadEthTxs()
                    .observe(this, ethTxEntities -> {
                        txEntityComparator = (o1, o2) -> {
                            if (o1.getSignId().equals(o2.getSignId())) {
                                return (int) (o2.getTimeStamp() - o1.getTimeStamp());
                            } else if (ELECTRUM_SIGN_ID.equals(o1.getSignId())) {
                                return -1;
                            } else {
                                return 1;
                            }
                        };
                        ethTxEntities = ethTxEntities.stream()
                                .filter(this::shouldShow)
                                .sorted(txEntityComparator)
                                .collect(Collectors.toList());
//                        adapter.setItems(ethTxEntities);
                    });
            txCallback = ethTx -> {
                String signedHex = ethTx.getSignedHex();
                Bundle bundle = new Bundle();
                bundle.putString(KEY_TX_ID, ethTx.getTxId());
                try {
                    new JSONObject(signedHex);
                    navigate(R.id.action_to_ethTxFragment, bundle);
                } catch (JSONException e) {
                    if (TextUtils.isEmpty(ethTx.getFee())) {
                        Log.i(TAG, "navigate: jump to ethEIP1559TxFragment");
                        navigate(R.id.action_to_ethTxFragment, bundle);
                    } else {
                        Log.i(TAG, "navigate: jump to new ethTxFragment");
                        navigate(R.id.action_to_ethTxFragment, bundle);
                    }

                    navigate(R.id.action_to_ethTxFragment, bundle);
                }
            };
        } else if (watchWallet == WatchWallet.POLKADOT_JS) {
            // do something
        }


        coinCode = requireArguments().getString(KEY_COIN_CODE);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!TextUtils.isEmpty(query) && adapter.getItemCount() == 0) {
                    mBinding.empty.setVisibility(View.VISIBLE);
                    mBinding.list.setVisibility(View.GONE);
                } else {
                    mBinding.empty.setVisibility(View.GONE);
                    mBinding.list.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean shouldShow(Tx tx) {
        boolean shouldShow;
        WatchWallet watchWallet = WatchWallet.getWatchWallet(mActivity);
        if (watchWallet.equals(WatchWallet.KEYSTONE)) {
             shouldShow = !tx.getSignId().contains("_sign_id");
        } else {
            shouldShow = tx.getSignId().equals(watchWallet.getSignId());
        }
        return shouldShow && Utilities.getCurrentBelongTo(mActivity).equals(tx.getBelongTo());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }

    public void setQuery(String s) {
        if (adapter != null) {
            query = s;
            adapter.getFilter().filter(s);
        }
    }

    class TxAdapter extends FilterableBaseBindingAdapter<Tx, TxListItemBinding> {

        TxAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.tx_list_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.POLKADOT_JS) {
                TxListItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
                Tx tx = items.get(position);
                if (!tx.getTxId().startsWith("0x")) {
                    Objects.requireNonNull(binding).txid.setText(coinCode + "-Transaction-" + (items.size() - position));
                } else {
                    Objects.requireNonNull(binding).txid.setText(tx.getTxId());
                }
            }
        }

        @Override
        protected void onBindItem(TxListItemBinding binding, Tx item) {
            binding.setTx(item);
            binding.txid.setText(item.getTxId());
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


