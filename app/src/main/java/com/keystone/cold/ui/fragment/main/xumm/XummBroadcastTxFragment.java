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

import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;

import org.json.JSONObject;

public class XummBroadcastTxFragment extends BroadcastTxFragment {
    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.broadcastHint.setText(R.string.broadcast_xrp_toolkit_hint);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public String getSignedTxData() {
        try {
            JSONObject object = new JSONObject(txEntity.getSignedHex());
            return object.getString("txHex");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
