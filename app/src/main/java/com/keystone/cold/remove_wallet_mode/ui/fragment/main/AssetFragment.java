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
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;

import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentAssetBinding;

import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.AddressNumberPicker;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;

import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.util.ViewUtils;
import com.keystone.cold.viewmodel.PolkadotViewModel;


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

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        AssetConfig config = AssetConfig.getConfigByCoinId(coinId);
        if (config.isShowAddAddress()) {
            binding.rlAddAddress.setVisibility(View.VISIBLE);
        }
        if (config.isShowChangePath()) {
            if (coinId.equals(Coins.BTC.coinId())) {
                binding.changePathText.setText(R.string.change_address_type);
            }
            binding.rlChangePath.setVisibility(View.VISIBLE);
        }
        if (config.isShowExportXPub()) {
            binding.rlExportXPUB.setVisibility(View.VISIBLE);
        }
        if (config.isShowFAQ()) {
            binding.rlFAQ.setVisibility(View.VISIBLE);
        }
        if (config.isShowResetDB()) {
            binding.rlResetDB.setVisibility(View.VISIBLE);
        }
        binding.rlAddAddress.setOnClickListener(v -> {
            handleAddAddress();
            dialog.dismiss();
        });
        binding.rlChangePath.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString(BundleKeys.COIN_ID_KEY, coinId);
            navigate(R.id.action_to_changeDerivationPathFragment, data);
            dialog.dismiss();
        });
        binding.rlExportXPUB.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString(BundleKeys.COIN_CODE_KEY, coinCode);
            navigate(R.id.action_from_assetFragment_to_exportXPUBFragment, data);
            dialog.dismiss();
        });
        binding.rlFAQ.setOnClickListener(v -> {
            //todo FAQ
            dialog.dismiss();
        });
        binding.rlResetDB.setOnClickListener(v -> {
            ModalDialog.showTwoButtonCommonModal(mActivity, getString(R.string.warning), getString(R.string.reset_polkadot_db_hint), getString(R.string.cancel), getString(R.string.confirm), dialog::dismiss, () -> {
                PolkadotViewModel viewModel = ViewModelProviders.of(mActivity).get(PolkadotViewModel.class);
                viewModel.resetDB();
            });
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    /**
     * handleAddAddress goes here
     * @param value how many accounts will be added;
     */
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
        BTC(Coins.BTC.coinId(), true, true, true, true, false),
        ETH(Coins.ETH.coinId(), true, true, true),
        APT(Coins.APTOS.coinId(), true, false, true),
        SOL(Coins.SOL.coinId(), true, true, true),
        SOL_BIP44_ROOT(Coins.SOL.coinId() + "_" + SOLAccount.SOLFLARE_BIP44_ROOT.getCode(), false, true, true),
        NEAR(Coins.NEAR.coinId(), false, true, true),
        NEAR_LEDGER(Coins.NEAR.coinId()+ "_" + NEARAccount.LEDGER.getCode(), true, true, true),
        DOT(Coins.DOT.coinId(), true, false, true, false, true),
        DEFAULT("default", true, true, true, false, true);

        private final String coinId;
        private final boolean showAddAddress;
        private final boolean showChangePath;
        private final boolean showFAQ;
        private final boolean showResetDB;
        private final boolean showExportXPub;

        AssetConfig(String coinId, boolean showAddAddress, boolean showChangePath, boolean showFAQ, boolean showExportXPub, boolean showResetDB) {
            this.coinId = coinId;
            this.showAddAddress = showAddAddress;
            this.showChangePath = showChangePath;
            this.showFAQ = showFAQ;
            this.showExportXPub = showExportXPub;
            this.showResetDB = showResetDB;
        }

        AssetConfig(String coinId, boolean showAddAddress, boolean showChangePath, boolean showFAQ) {
            this(coinId, showAddAddress, showChangePath, showFAQ, false, false);
        }

        public static AssetConfig getConfigByCoinId(String coinId) {
            if (Coins.SOL.coinId().equals(coinId)) {
                String code = Utilities.getCurrentSolAccount(MainApplication.getApplication());
                if (SOLAccount.SOLFLARE_BIP44_ROOT.getCode().equals(code)) {
                    coinId = coinId + "_" + code;
                }
            } else if (Coins.NEAR.coinId().equals(coinId)) {
                String code = Utilities.getCurrentNearAccount(MainApplication.getApplication());
                if (NEARAccount.LEDGER.getCode().equals(code)) {
                    coinId = coinId + "_" + code;
                }
            }
            String finalCoinId = coinId;
            Optional<AssetConfig> config = Arrays.stream(AssetConfig.values()).filter(assetConfig -> assetConfig.coinId.equals(finalCoinId)).findFirst();
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

        public boolean isShowResetDB() {
            return showResetDB;
        }

        public boolean isShowExportXPub() {
            return showExportXPub;
        }
    }
}
