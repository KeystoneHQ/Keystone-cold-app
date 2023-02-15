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

package com.keystone.cold.ui.fragment.main.arweave;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveTxBinding;
import com.keystone.cold.databinding.PolkadotTxBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ArweaveTxFragment extends BaseFragment<ArweaveTxBinding> {
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.arweave_tx;
    }

    private Fragment[] fragments;

    private String rawTx;
    private ArweaveTransaction parsedTx;
    private String signature;
    private String requestId;
    private UR ur;

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle bundle = requireArguments();
        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                try {
                    JSONObject signedRawTx = new JSONObject(txEntity.getSignedHex());
                    JSONObject object = new JSONObject(txEntity.getAddition());
                    rawTx = signedRawTx.toString(2);
                    parsedTx = ArweaveTransaction.fromJSON(signedRawTx);
                    signature = object.getString("signature");
                    requestId = object.getString("requestId");
                    UUID uuid = UUID.fromString(requestId);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
                    byteBuffer.putLong(uuid.getMostSignificantBits());
                    byteBuffer.putLong(uuid.getLeastSignificantBits());
                    byte[] requestId = byteBuffer.array();
                    ArweaveSignature arweaveSignature = new ArweaveSignature(Hex.decode(signature), requestId);
                    ur = arweaveSignature.toUR();
                    initViewPager();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initViewPager() {
        String[] title = {getString(R.string.overview), getString(R.string.raw), getString(R.string.qr)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = ArweaveParsedTxFragment.newInstance(parsedTx);
            fragments[1] = ArweaveTxDetailFragment.newInstance(rawTx);
            fragments[2] = URFragment.newInstance(ur);
        }
        mBinding.viewPager.setOffscreenPageLimit(3);
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
