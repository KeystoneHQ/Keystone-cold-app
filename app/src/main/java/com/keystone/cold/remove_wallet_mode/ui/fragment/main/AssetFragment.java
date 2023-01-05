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

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;

import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentAssetBinding;

import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.AddressNumberPicker;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;

import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.util.ViewUtils;


import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class AssetFragment extends BaseFragment<FragmentAssetBinding> implements NumberPickerCallback {

    public static final String TAG = "AssetFragment";

    private Fragment[] fragments;
    private String coinId;
    private String coinCode;
    private AddressNumberPicker addressNumberPicker;

    @Override
    protected int setView() {
        return R.layout.fragment_asset;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        Bundle data = requireArguments();
        coinId = data.getString(KEY_COIN_ID);
        coinCode = data.getString(KEY_COIN_CODE);
        mBinding.setCoinCode(coinCode);

        initViewPager();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.more, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initViewPager() {
        String[] title = {getString(R.string.tab_my_address), getString(R.string.tab_transaction_history)};
        if (fragments == null) {
            fragments = new Fragment[title.length];

            fragments[0] = AddressFragment.newInstance(coinId, coinCode);
            fragments[1] = TxListFragment.newInstance(coinId, coinCode);
        }

        mBinding.viewpager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
    }

    private void showBadge(MenuItem menuItem) {
        Drawable menu = Objects.requireNonNull(menuItem).getIcon();
        int badgeSize = (int) getResources().getDimension(R.dimen.default_badge_size);
        Drawable menuWithBadge = ViewUtils.addBadge(getResources(), menu, badgeSize);
        menuItem.setIcon(menuWithBadge);
    }

    private void hideBadge() {
        MenuItem menuItem = mBinding.toolbar.getMenu().findItem(R.id.action_more);
        menuItem.setIcon(R.drawable.more);
    }

    private BadgeView generateBadgeView(View anchor) {
        return BadgeFactory.create(anchor.getContext())
                .setWidthAndHeight(10, 10)
                .setBadgeBackground(Color.RED)
                .setBadgeGravity(Gravity.END | Gravity.TOP)
                .setShape(BadgeView.SHAPE_CIRCLE)
                .setSpace(10, 0)
                .bind(anchor);
    }

    @Override
    public void onValueSet(int value) {
        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler(MainApplication.getApplication().getMainLooper());
        Runnable runnable = () -> handler.postDelayed(dialog::dismiss, UIConstants.DIALOG_DISMISS_DELAY_TIME);
        AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), coinId);
        AddressViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(AddressViewModel.class);
        LiveData<Boolean> complete = viewModel.addAddress(value);
        complete.observe(AssetFragment.this, result -> {
            runnable.run();
            complete.removeObservers(AssetFragment.this);
        });
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        AssetConfig config = AssetConfig.getConfigByCoinId(coinId);
        if (config.isShowAddAddress()) {
            binding.rlAddAddress.setVisibility(View.VISIBLE);
        }
        if (config.isShowChangePath()) {
            binding.rlChangePath.setVisibility(View.VISIBLE);
        }
        if (config.isShowFAQ()) {
            binding.rlFAQ.setVisibility(View.VISIBLE);
        }
        binding.rlAddAddress.setOnClickListener(v -> {
            handleAddAddress();
            dialog.dismiss();
        });
        binding.rlChangePath.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString(KEY_COIN_ID, coinId);
            navigate(R.id.action_to_changeDerivationPathFragment, data);
            dialog.dismiss();
        });
        binding.rlFAQ.setOnClickListener(v -> {
            //todo FAQ
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void handleAddAddress() {
        if (fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).exitEditAddressName();
        }
        if (addressNumberPicker == null) {
            addressNumberPicker = new AddressNumberPicker();
            addressNumberPicker.setCallback(this);
        }
        addressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
    }

    private enum AssetConfig {
        ETH(Coins.ETH.coinId(), true, true, true),
        DEFAULT("default", true, true, true),
        ;

        private String coinId;
        private boolean showAddAddress;
        private boolean showChangePath;
        private boolean showFAQ;

        AssetConfig(String coinId, boolean showAddAddress, boolean showChangePath, boolean showFAQ) {
            this.coinId = coinId;
            this.showAddAddress = showAddAddress;
            this.showChangePath = showChangePath;
            this.showFAQ = showFAQ;
        }

        public static AssetConfig getConfigByCoinId(String coinId) {
            Optional<AssetConfig> config = Arrays.stream(AssetConfig.values()).filter(assetConfig -> assetConfig.coinId.equals(coinId)).findFirst();
            return config.orElse(DEFAULT);
        }

        public boolean isShowAddAddress() {
            return showAddAddress;
        }

        public boolean isShowChangePath() {
            return showChangePath;
        }

        public boolean isShowFAQ() {
            return showFAQ;
        }
    }
}
