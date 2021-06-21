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

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.PublicKeyFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.PublicKeyViewModel;

import java.util.Objects;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

public class PublicKeyFragment extends BaseFragment<PublicKeyFragmentBinding> {

    private String coinId;
    private String pubKey;
    private String address;

    static Fragment newInstance(@NonNull String coinId) {
        PublicKeyFragment fragment = new PublicKeyFragment();
        Bundle args = new Bundle();
        args.putString(KEY_COIN_ID, coinId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.public_key_fragment;
    }

    @Override
    protected void init(View view) {
        coinId = requireArguments().getString(KEY_COIN_ID);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        PublicKeyViewModel viewModel = ViewModelProviders.of(this)
                .get(PublicKeyViewModel.class);
        final Handler handler = new Handler();
        mBinding.setCoinCode(Coins.coinCodeFromCoinId(coinId));
        AppExecutors.getInstance().diskIO().execute(() -> {
            address = viewModel.getAddress(coinId);
            handler.post(() -> {
                if (TextUtils.isEmpty(address)) {
                    mBinding.publicKeyPage.setVisibility(View.VISIBLE);
                    mBinding.addressLayout.addressPage.setVisibility(View.GONE);
                    viewModel.calcPubKey(coinId)
                            .observe(this, pubKey -> {
                                mBinding.setPubKey(pubKey);
                                this.pubKey = pubKey;
                            });
                } else {
                    mBinding.publicKeyPage.setVisibility(View.GONE);
                    mBinding.addressLayout.addressPage.setVisibility(View.VISIBLE);
                    mBinding.setAddress(address);
                }
            });
        });


    }

}
