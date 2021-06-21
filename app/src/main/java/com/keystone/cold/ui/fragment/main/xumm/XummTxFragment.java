
/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.ui.fragment.main.xumm;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.XummTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

public class XummTxFragment extends BaseFragment<XummTxBinding> {

    private Fragment[] fragments;
    private Bundle bundle;
    private JSONObject tx;
    private String txHex;

    @Override
    protected int setView() {
        return R.layout.xumm_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        bundle = requireArguments();

        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            try {
                tx = new JSONObject(txEntity.getSignedHex());
                txHex = tx.getString("txHex");
                tx.remove("txHex");
                initViewPager();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void initViewPager() {
        String[] title = { getString(R.string.simple), getString(R.string.raw)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = XummTxDetailFragment.newInstance(tx.toString(), txHex);
            fragments[1] = XummRawTxFragment.newInstance(tx.toString());
        }

        mBinding.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
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
        mBinding.tab.setupWithViewPager(mBinding.viewPager);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
