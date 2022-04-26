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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.SetupWatchWalletBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.SetupVaultActivity;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;

public class SetupWatchWalletFragment extends SetupVaultBaseFragment<SetupWatchWalletBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_watch_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> onConfirm());
    }

    private void onConfirm() {
//        WatchWallet watchWallet = WatchWallet.getWatchWallet(mActivity);
//        Bundle data = new Bundle();
//        data.putBoolean(IS_SETUP_VAULT, ((SetupVaultActivity) mActivity).inSetupProcess);
//        switch (watchWallet) {
//            case KEYSTONE:
//                navigate(R.id.action_to_manageCoinFragment, data);
//                break;
//            case METAMASK:
//            case XRP_TOOLKIT:
//            case SOLANA:
//                navigate(R.id.action_to_syncWatchWalletGuide, data);
//                break;
//            case POLKADOT_JS:
//                data.putString("coinCode", Coins.DOT.coinCode());
//                navigate(R.id.action_to_manageCoinFragment, data);
//                break;
//        }

        stepIntoMainActivity();
    }

    private void stepIntoMainActivity(){
        startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();
    }
}
