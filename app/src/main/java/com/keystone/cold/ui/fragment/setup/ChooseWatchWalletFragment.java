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

package com.keystone.cold.ui.fragment.setup;

import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.SettingItemSelectableBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.SetupVaultActivity;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.setting.ListPreferenceFragment;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.ArrayList;
import java.util.List;

public class ChooseWatchWalletFragment extends ListPreferenceFragment {

    public static final String TAG = "ChooseWatchWalletFragment";
    private Adapter adapter;
    private List<Pair<String, String>> displayItems;

    private String tempValue;

    @Override
    protected void init(View view) {
        if (mActivity instanceof MainActivity) {
            mBinding.toolbarTitle.setText(R.string.choose_watch_only_wallet);
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
            mBinding.button.setVisibility(View.VISIBLE);
            mBinding.button.setOnClickListener(v -> onChooseWallet());
            mBinding.button.setEnabled(true);
        } else {
            mBinding.toolbar.setVisibility(View.GONE);
        }
        prefs = Utilities.getPrefs(mActivity);
        entries = getResources().getStringArray(getEntries());
        values = getResources().getStringArray(getValues());
        subTitles = getResources().getStringArray(R.array.watch_wallet_summury);
        value = prefs.getString(getKey(), defaultValue());
        tempValue = value;
        adapter = new Adapter(mActivity);

        displayItems = new ArrayList<>();
        for (int i = 0; i < entries.length; i++) {
            displayItems.add(Pair.create(values[i].toString(), entries[i].toString()));
        }

        adapter.setItems(displayItems);
        mBinding.list.setAdapter(adapter);
    }

    private void onChooseWallet() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Utilities.IS_SWITCH_WATCH_WALLET, true);
        switch (WatchWallet.getWatchWalletById(tempValue)) {
            case POLKADOT_JS:
                bundle.putString("coinCode", Coins.DOT.coinCode());
                navigate(R.id.action_to_manageCoinFragment, bundle);
                updateCurrentWatchWallet();
                break;
            case KEYSTONE:
                navigate(R.id.action_to_manageCoinFragment, bundle);
                updateCurrentWatchWallet();
                break;
            case METAMASK:
//                int web3GuideTimes = Utilities.getWeb3GuideTimes(mActivity);
//                if (web3GuideTimes > 0 && mActivity instanceof MainActivity) {
//                    navigate(R.id.action_to_home);
//                } else {
//                    navigate(R.id.action_to_syncWatchWalletGuide, bundle);
//                }
                //navigate(R.id.action_to_syncWatchWalletGuide, bundle);
                updateCurrentWatchWallet();
                stepIntoMainActivity();
                break;
            case XRP_TOOLKIT:
                //navigate(R.id.action_to_syncWatchWalletGuide, bundle);
                updateCurrentWatchWallet();
                stepIntoMainActivity();
                break;
            case SOLANA:
                updateCurrentWatchWallet();
                stepIntoMainActivity();
                break;
        }
    }

    private void stepIntoMainActivity(){
        startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();
    }

    private void updateCurrentWatchWallet() {
        value = tempValue;
        prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, value).apply();
    }

    protected String getEntryByPosition(int position) {
        return displayItems.get(position).second;
    }

    protected String getValueByPosition(int position) {
        return displayItems.get(position).first;
    }

    @Override
    protected int getEntries() {
        return R.array.watch_wallet_list;
    }

    @Override
    protected int getValues() {
        return R.array.watch_wallet_list_values;
    }

    @Override
    protected String getKey() {
        return SETTING_CHOOSE_WATCH_WALLET;
    }

    @Override
    protected String defaultValue() {
        return WatchWallet.KEYSTONE.getWalletId();
    }

    @Override
    public void onSelect(int index) {
        if (mActivity instanceof MainActivity) {
            String old = tempValue;
            tempValue = getValueByPosition(index);
            if (!TextUtils.equals(old, tempValue)) {
                adapter.notifyDataSetChanged();
            }
        } else {
            String old = tempValue;
            value = getValueByPosition(index);
            if (!TextUtils.equals(old, value)) {
                tempValue = value;
                prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, value).apply();
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected class Adapter extends BaseBindingAdapter<Pair<String, String>, SettingItemSelectableBinding> {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.setting_item_selectable;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SettingItemSelectableBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.title.setText(getEntryByPosition(position));
            binding.title.setTypeface(binding.title.getTypeface(), Typeface.BOLD);

            binding.subTitle.setVisibility(View.VISIBLE);
            binding.subTitle.setText(subTitles[position]);
            binding.subTitle.setTextColor(mActivity.getColor(R.color.white40));
            binding.setIndex(position);
            binding.setCallback(ChooseWatchWalletFragment.this);
            if (mActivity instanceof MainActivity) {
                binding.setChecked(getValueByPosition(position).equals(tempValue));
            } else if (mActivity instanceof SetupVaultActivity) {
                binding.setChecked(getValueByPosition(position).equals(value));
            }
        }

        @Override
        protected void onBindItem(SettingItemSelectableBinding binding, Pair<String, String> item) {
        }
    }
}

