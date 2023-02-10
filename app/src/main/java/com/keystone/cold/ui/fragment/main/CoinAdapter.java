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
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AssetItemBinding;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.viewmodel.CoinModel;
import com.keystone.cold.model.ui.AssetItem;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;
import com.keystone.cold.viewmodel.WatchWallet;

public class CoinAdapter extends FilterableBaseBindingAdapter<CoinEntity, AssetItemBinding> {

    private final CoinClickCallback mCoinClickCallback;
    private final boolean isManageCoin;
    private final Context context;

    public CoinAdapter(Context context, @Nullable CoinClickCallback clickCallback, boolean isManageCoin) {
        super(context);
        this.context = context;
        mCoinClickCallback = clickCallback;
        this.isManageCoin = isManageCoin;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.asset_item;
    }

    @Override
    protected void onBindItem(AssetItemBinding binding, CoinEntity item) {
        if (WatchWallet.getWatchWallet(context) != WatchWallet.POLKADOT_JS) {
            binding.setIsManage(isManageCoin);
        }
        if (WatchWallet.getWatchWallet(context).equals(WatchWallet.POLKADOT_JS) && !isManageCoin
        ) {
            if (item.getCoinCode().equals(Coins.DOT.coinCode()) && !Utilities.hasUserClickPolkadotSyncLock(context)) {
                generateBadgeView(binding.addr);
            } else if (item.getCoinCode().equals(Coins.KSM.coinCode()) && !Utilities.hasUserClickKusamaSyncLock(context)) {
                generateBadgeView(binding.addr);
            }
        }
        binding.setCallback(mCoinClickCallback);
        CoinModel coinModel = item.toCoinModel();
        binding.setCoin(coinModel);
        binding.setAsset(adaptCoinModelToAssetItem(coinModel));
        if (isManageCoin || Coins.showPublicKey(item.getCoinCode())) {
            binding.addrNum.setVisibility(View.GONE);
            binding.addr.setVisibility(View.GONE);
        }
    }

    private void generateBadgeView(View anchor) {
        BadgeFactory.create(anchor.getContext())
                .setWidthAndHeight(10, 10)
                .setBadgeBackground(Color.RED)
                .setBadgeGravity(Gravity.END | Gravity.TOP)
                .setShape(BadgeView.SHAPE_CIRCLE)
                .setSpace(10, 0)
                .bind(anchor);
    }

    private AssetItem adaptCoinModelToAssetItem(CoinModel coinModel) {
        if (WatchWallet.getWatchWallet(context).equals(WatchWallet.CORE_WALLET)) {
            if (coinModel.getCoinCode().equals(Coins.BTC_CORE_WALLET.coinCode())) {
                return new AssetItem(Coins.BTC_CORE_WALLET.coinName(), coinModel.getCoinCode(), coinModel.getCoinCode(), "", false, coinModel.getAddressCount());
            } else {
                // ETH, but we treat as AVAX here;
                return new AssetItem(Coins.AVAX.coinName(),
                        Coins.AVAX.coinCode(),
                        Coins.AVAX.coinCode(),
                        "",
                        false, coinModel.getAddressCount());
            }
        } else if (WatchWallet.getWatchWallet(context).equals(WatchWallet.BIT_KEEP)) {
            if (coinModel.getCoinCode().equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) {
                return new AssetItem("BTC", coinModel.getCoinCode(), coinModel.getCoinCode(), "", false, coinModel.getAddressCount());
            }
        } else if (WatchWallet.getWatchWallet(context).equals(WatchWallet.KEPLR_WALLET)) {
            return new AssetItem(coinModel.getName(),
                    coinModel.getDisplayName(),
                    coinModel.getCoinCode(),
                    coinModel.getTag(),
                    coinModel.hasTag(), coinModel.getAddressCount());
        } else if (WatchWallet.getWatchWallet(context).equals(WatchWallet.KEYSTONE) && (coinModel.getCoinCode().equals(Coins.DOT.coinCode()) || coinModel.getCoinCode().equals(Coins.KSM.coinCode()))) {
            return new AssetItem(coinModel.getDisplayCoinCode(),
                    coinModel.getDisplayName(),
                    coinModel.getCoinCode(),
                    coinModel.getTag(),
                    coinModel.hasTag(), 1);
        }
        return new AssetItem(coinModel.getDisplayCoinCode(),
                coinModel.getDisplayName(),
                coinModel.getCoinCode(),
                coinModel.getTag(),
                coinModel.hasTag(), coinModel.getAddressCount());

    }
}
