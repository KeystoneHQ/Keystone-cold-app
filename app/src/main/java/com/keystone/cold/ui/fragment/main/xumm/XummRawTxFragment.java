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

import com.keystone.cold.R;
import com.keystone.cold.databinding.XrpTxDetailRawBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class XummRawTxFragment extends BaseFragment<XrpTxDetailRawBinding> {

    public static final String XRP_TX = "xrp_tx";

    static Fragment newInstance(@NonNull String json) {
        XummRawTxFragment fragment = new XummRawTxFragment();
        Bundle args = new Bundle();
        args.putString(XRP_TX, json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.xrp_tx_detail_raw;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        try {
            JSONObject tx = new JSONObject(bundle.getString(XRP_TX));
            mBinding.rawTx.setText(tx.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
