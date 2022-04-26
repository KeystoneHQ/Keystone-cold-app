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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.SyncWatchWalletGuideBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.viewmodel.WatchWallet;


public class SyncWatchWalletGuide extends SetupVaultBaseFragment<SyncWatchWalletGuideBinding> {

    private WatchWallet watchWallet;
    private String coinCode;

    @Override
    protected int setView() {
        return R.layout.sync_watch_wallet_guide;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbarTitle.setText(R.string.sync_watch_wallet);
        coinCode = requireArguments().getString("coinCode");
        if (!(mActivity instanceof MainActivity)) {
            mBinding.skip.setText(R.string.sync_later);
        }
        mBinding.skip.setOnClickListener(v -> {
            startActivity(new Intent(mActivity, MainActivity.class));
            mActivity.finish();
        });

        mBinding.text1.setText(getString(getSyncWatchWalletGuideTitle(watchWallet), coinCode));
        mBinding.text2Hint.setVisibility(View.GONE);
        mBinding.text2.setText(getString(getSyncWatchWalletGuide(watchWallet),
                Coins.coinNameFromCoinCode(coinCode), coinCode));

        mBinding.text3.setText(getText3());
        mBinding.text4.setText(getText4());
        mBinding.export.setText(getButtonText());
        mBinding.export.setOnClickListener(v -> export());
    }

    private String getText3() {
        if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            mBinding.text3.setVisibility(View.VISIBLE);
            return getString(R.string.sync_first_xrp_accout);
        }
        return "";
    }

    private String getText4() {
        if (watchWallet == WatchWallet.METAMASK) {
            mBinding.text4.setVisibility(View.VISIBLE);
            return getString(R.string.sync_metamask_hit);
        }
        return "";
    }

    private void export() {
        switch (watchWallet) {
            case KEYSTONE:
            case XRP_TOOLKIT:
            case POLKADOT_JS:
            case METAMASK:
            case SOLANA:
                Bundle bundle = requireArguments();
                bundle.putBoolean("fromSyncGuide", true);
                navigate(R.id.action_to_syncFragment, bundle);
                break;
        }
    }

    @SuppressLint("StringFormatInvalid")
    private String getButtonText() {
        int id = 0;
        switch (watchWallet) {
            case KEYSTONE:
                id = R.string.sync_keystone_guide_button_text;
                break;
            case XRP_TOOLKIT:
                id = R.string.sync_xrp_toolkit_guide_button_text;
                break;
            case POLKADOT_JS:
                id = R.string.sync_polkadot_js_guide_button_text;
                break;
            case METAMASK:
                id = R.string.sync_metamask_guide_button_text;
                break;
            case SOLANA:
                id = R.string.sync_metamask_guide_button_text;
                break;
        }
        return getString(id, coinCode);
    }

    public static int getSyncWatchWalletGuideTitle(WatchWallet watchWallet) {
        switch (watchWallet) {
            case KEYSTONE:
                return R.string.sync_keystone_wallet_guide_title;
            case XRP_TOOLKIT:
                return R.string.sync_xrp_toolkit_guide_title;
            case POLKADOT_JS:
                return R.string.sync_polkadotjs_wallet_guide_title;
            case METAMASK:
                return R.string.sync_metamask_guide_title;
            case SOLANA:
                return R.string.sync_solana_guide_title;
        }
        return 0;
    }

    public static int getSyncWatchWalletGuide(WatchWallet watchWallet) {
        switch (watchWallet) {
            case KEYSTONE:
                return R.string.sync_keystone_wallet_guide_text;
            case XRP_TOOLKIT:
                return R.string.sync_xrp_toolkit_guide_text;
            case POLKADOT_JS:
                return R.string.sync_polkadot_js_guide_text;
            case METAMASK:
                return R.string.sync_metamask_guide_text;
            case SOLANA:
                return R.string.sync_metamask_guide_text;
        }
        return 0;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
