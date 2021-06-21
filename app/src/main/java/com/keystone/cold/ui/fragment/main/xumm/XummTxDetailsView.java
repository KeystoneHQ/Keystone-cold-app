
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.XrpTxItemBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XummTxDetailsView extends LinearLayout {
    private JSONObject tx;

    public XummTxDetailsView(Context context) {
        this(context, null);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet, int i, int i1) {
        super(context, attributeSet, i, i1);
    }

    public void setData(JSONObject object) {
        tx = object;
        showTransactionDetails(tx);
    }

    private void showTransactionDetails(JSONObject tx) {
        removeAllViews();
        Map<String, String> map = toMap(tx);
        List<String> sortedKeys = new ArrayList<>(map.keySet());
        sortedKeys.sort((o1, o2) -> getDisplayOrder(o1) - getDisplayOrder(o2));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String key : sortedKeys) {
            XrpTxItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.xrp_tx_item, null, false);
            binding.title.setText(key + ":");
            binding.content.setText(map.get(key));
            addView(binding.getRoot());
        }
    }

    public Map<String, String> toMap(JSONObject jsonObj) {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObj.get(key);
                map.put(key, value.toString());
            }
            return map;
        } catch (JSONException ignored) {

        }
        return null;
    }

    private int getDisplayOrder(final String key) {
        int index = keys.indexOf(key);
        if (index != -1) {
            return index;
        }
        if (key.equals("Fee")) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE - 1;
        }
    }

    List<String> keys = Arrays.asList(
            "TransactionType",
            "Account",
            "SourceTag",
            "Amount",
            "Destination",
            "DestinationTag",
            "Flags",
            "CheckID",
            "EmailHash",
            "SendMax",
            "DeliverMin",
            "FinishAfter",
            "CancelAfter",
            "TakerPays",
            "Amount.currency",
            "Amount.value",
            "Amount.issuer",
            "SendMax.currency",
            "SendMax.value",
            "SendMax.issuer",

            "DeliverMin.currency",
            "DeliverMin.value",
            "DeliverMin.issuer",

            "LimitAmount.currency",
            "LimitAmount.value",
            "LimitAmount.issuer",

            "TakerGets.currency",
            "TakerGets.value",
            "TakerGets.issuer",

            "TakerPays.currency",
            "TakerPays.value",
            "TakerPays.issuer",

            "Owner",
            "OfferSequence",
            "Domain",
            "Flags",
            "Memos",
            "Expiration"
    );
}
