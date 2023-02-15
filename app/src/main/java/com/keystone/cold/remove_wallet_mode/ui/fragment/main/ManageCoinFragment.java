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


import android.os.Bundle;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentManageCoinBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.remove_wallet_mode.ui.adapter.CoinAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AssetViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageCoinFragment extends BaseFragment<FragmentManageCoinBinding> {

    public static final String TAG = "ManageCoinFragment";
    private CoinAdapter mCoinAdapter;
    private AssetViewModel mViewModel;
    private final List<AssetItem> assetItems = new ArrayList<>();


    @Override
    protected int setView() {
        return R.layout.fragment_manage_coin;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            navigateUp();
            if (!assetItems.isEmpty()) {
                mViewModel.toggleAssetItem(assetItems);
            }
        });
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mCoinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, true);
        mBinding.assetList.setAdapter(mCoinAdapter);
        mBinding.ivConfirm.setOnClickListener(v -> navigateUp());
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(mActivity).get(AssetViewModel.class);
        subscribeUi(mViewModel.loadAssets());
    }

    private void subscribeUi(LiveData<List<AssetItem>> assets) {
        assets.observe(this, assetItems -> {
            if (assetItems != null) {
                mCoinAdapter.setItems(assetItems);
                if (assetItems.stream().anyMatch(a -> a.getCoinId().equals(Coins.AR.coinId()))) {
                    ArweaveViewModel viewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
                    LiveData<Boolean> hasAR = viewModel.hasArweaveAddress();
                    hasAR.observe(this, (v) -> {
                        if (!v) {
                            navigate(R.id.action_from_manageCoinFragment_to_ArweaveAuthFragment);
                        }
                        hasAR.removeObservers(this);
                    });
                }
            }
        });
    }

    private final CoinAdapter.CoinClickCallback mCoinClickCallback = assetItem -> {
        if (assetItems.contains(assetItem)) {
            assetItems.remove(assetItem);
        } else {
            assetItems.add(assetItem);
        }
        mViewModel.toggleAssetItem(assetItem);
    };

    @Override
    public void onDestroyView() {
        mViewModel.loadAssets().removeObservers(this);
        super.onDestroyView();
    }
}


