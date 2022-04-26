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

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.Constants.KEY_ID;
import static com.keystone.cold.viewmodel.CoinListViewModel.coinEntityComparator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AssetListBottomMenuBinding;
import com.keystone.cold.databinding.AssetListFragmentBinding;
import com.keystone.cold.db.PresetData;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssetListFragment extends BaseFragment<AssetListFragmentBinding> {

    public static final String TAG = "AssetListFragment";

    private CoinAdapter mCoinAdapter;
    private WatchWallet watchWallet;


    @Override
    protected int setView() {
        return R.layout.asset_list_fragment;
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.METAMASK ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.SOLANA ) {
            navigate(R.id.assetFragment);
        }
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
        mCoinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, false);
        mBinding.assetList.setAdapter(mCoinAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinListViewModel mViewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        subscribeUi(mViewModel.getCoins());
        checkAndAddNewCoins();
    }

    private void checkAndAddNewCoins() {
        SetupVaultViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(SetupVaultViewModel.class);
        AppExecutors.getInstance().diskIO().execute(()
                -> viewModel.presetData(PresetData.generateCoins(mActivity), null)
        );
    }

    private void subscribeUi(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, coinEntities -> {
            if (coinEntities != null) {
                List<CoinEntity> toShow = filterDisplayCoins(coinEntities);
                if (toShow.isEmpty()) {
                    mBinding.setIsEmpty(true);
                } else {
                    mBinding.setIsEmpty(false);
                    toShow.sort(coinEntityComparator);
                    mCoinAdapter.setItems(toShow);
                }

            } else {
                mBinding.setIsEmpty(true);
            }
            mBinding.executePendingBindings();
        });
    }

    private List<CoinEntity> filterDisplayCoins(List<CoinEntity> coinEntities) {
        Stream<CoinEntity> filterStream = filterSupportedCoin(coinEntities, watchWallet);
        if (watchWallet == WatchWallet.KEYSTONE) {
            filterStream = filterStream.filter(CoinEntity::isShow);
        }
        return filterStream.collect(Collectors.toList());
    }

    public static Stream<CoinEntity> filterSupportedCoin(List<CoinEntity> coinEntities, WatchWallet watchWallet) {
        Coins.Coin[] supportedCoins = watchWallet.getSupportedCoins();
        return coinEntities.stream()
                .filter(c -> Arrays.stream(supportedCoins)
                        .anyMatch(coin -> coin.coinCode().equals(c.getCoinCode()))
                );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        if (watchWallet != WatchWallet.KEYSTONE) {
            MenuItem item = menu.findItem(R.id.action_more);
            item.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            AndPermission.with(this)
                    .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                    .onGranted(permissions -> navigate(R.id.action_to_scan))
                    .onDenied(permissions -> {
                        Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(mActivity, getString(R.string.scan_permission_denied), Toast.LENGTH_LONG).show();
                    }).start();
            return true;
        }

        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        AssetListBottomMenuBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.asset_list_bottom_menu,null,false);
        binding.addHideAsset.setOnClickListener(v-> {
            navigate(R.id.action_to_manageCoinFragment);
            dialog.dismiss();

        });
        binding.sync.setOnClickListener(v-> {
            navigate(R.id.action_to_syncFragment);
            dialog.dismiss();

        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private final CoinClickCallback mCoinClickCallback = coin -> {
        Bundle data = new Bundle();
        data.putLong(KEY_ID, coin.getId());
        data.putString(KEY_COIN_ID, coin.getCoinId());
        data.putString(KEY_COIN_CODE, coin.getCoinCode());
        navigate(R.id.action_to_assetFragment, data);
    };
}
