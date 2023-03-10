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

package com.keystone.cold.remove_wallet_mode.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.keystone.cold.R;
import com.keystone.cold.databinding.ItemAssetListBinding;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.ui.common.FilterableBaseBindingAdapter;

public class CoinAdapter extends FilterableBaseBindingAdapter<AssetItem, ItemAssetListBinding> {

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
        return R.layout.item_asset_list;
    }

    @Override
    protected void onBindItem(ItemAssetListBinding binding, AssetItem item) {
        binding.setIsManage(isManageCoin);
        binding.setCallback(mCoinClickCallback);
        binding.setAssetItem(item);
        binding.etvEco.setData(item.getEcology());

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


    public interface CoinClickCallback {
        void onClick(AssetItem assetItem);
    }
}
