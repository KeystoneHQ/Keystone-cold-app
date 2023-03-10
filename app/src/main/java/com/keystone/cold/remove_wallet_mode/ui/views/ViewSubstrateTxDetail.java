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

package com.keystone.cold.remove_wallet_mode.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ParamItemBinding;
import com.keystone.cold.databinding.ViewSubstrateTxDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ViewSubstrateTxDetail extends ScrollView {
    private final LayoutInflater inflater;
    private ViewSubstrateTxDetailBinding mBinding;
    private List<Card> cards = new ArrayList<>();
    private Context mContext;

    public ViewSubstrateTxDetail(Context context) {
        this(context, null);
    }

    public ViewSubstrateTxDetail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewSubstrateTxDetail(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewSubstrateTxDetail(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void updateUI(JSONArray content) throws JSONException {
        updateUI(content, false);
    }

    private JSONArray filterErrorCard(JSONArray content) throws JSONException {
        int length = content.length();
        JSONArray result = new JSONArray();
        for (int i = 0; i < length; i++) {
            JSONObject object = content.getJSONObject(i);
            String type = object.getJSONObject("card").getString("type");

            if(type.equals("Warning") || type.equals("Error")) {
                result.put(object);
            }
        }
        return result;
    }

    public void updateUI(JSONArray content, boolean inError) throws JSONException {
        mBinding = DataBindingUtil.getBinding(this);
        JSONArray jsonCards = content;
        if (inError) {
            jsonCards = filterErrorCard(content);
        }
        int length = jsonCards.length();

        for (int i = 0; i < length; i++) {
            cards.add(Card.fromJSON(jsonCards.getJSONObject(i)));
        }
        List<Card> sortedCards = cards.stream().sorted(Comparator.comparingInt(v -> v.index)).collect(Collectors.toList());
        Iterator<Card> iterator = sortedCards.iterator();
        while (iterator.hasNext()) {
            addCard(iterator.next(), inError);
            if (iterator.hasNext()) {
                addDivider();
            }
        }
    }

    private void addDivider() {
        @SuppressLint("InflateParams")
        View div = inflater.inflate(R.layout.divider, null, false);
        mBinding.container.addView(div);
    }

    @SuppressLint("ResourceAsColor")
    private void addCard(Card card, boolean inError) {
        int paddingBase = 8;
        ParamItemBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.param_item, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                paddingBase * (card.indent),
                mContext.getResources().getDisplayMetrics()
        );
        binding.wrapper.setPadding(padding, 0, 0, 0);
        binding.wrapper.setLayoutParams(layoutParams);
        binding.key.setText(card.getTitle());
        if (card.getValue() == null) {
            binding.value.setVisibility(GONE);
        }
        else {
            binding.value.setText(card.getValue());
        }
        if(inError){
            binding.key.setVisibility(GONE);
            binding.key.setTextColor(R.color.black);
            binding.value.setTextColor(R.color.black);
            binding.value.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }
        mBinding.container.addView(binding.getRoot());
    }

    static class Card {
        private final int index;
        private final int indent;
        private final String title;
        private final String value;

        public Card(int index, int indent, String title, String value) {
            this.index = index;
            this.indent = indent;
            this.title = title;
            this.value = value;
        }

        public static Card fromJSON(JSONObject json) throws JSONException {
            int index = json.getInt("index");
            int indent = json.getInt("indent");
            JSONObject card = json.getJSONObject("card");
            String title = card.getString("type");
            Object content = card.opt("value");
            String value;
            if (content == null) {
                return new Card(index, indent, title, null);
            }
            if (content instanceof JSONObject) {
                JSONObject j = (JSONObject) content;
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<String> ki = j.keys();
                while (ki.hasNext()) {
                    String key = ki.next();
                    String v = j.get(key).toString();
                    stringBuilder.append(key).append(": ").append(v);
                    stringBuilder.append("\n");
                }
                value = stringBuilder.toString();
            } else {
                value = content.toString();
            }

            return new Card(index, indent, title, value);
        }

        public int getIndex() {
            return index;
        }

        public int getIndent() {
            return indent;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }
    }
}
