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

package com.keystone.cold.ui.common;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.databinding.ViewDataBinding;

import com.keystone.cold.db.entity.FilterableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FilterableBaseBindingAdapter<M extends FilterableItem, B extends ViewDataBinding>
        extends BaseBindingAdapter<M, B> implements Filterable {

    protected final List<M> allItems = new ArrayList<>();

    public FilterableBaseBindingAdapter(Context context) {
        super(context);
    }

    @Override
    public void setItems(List<M> data) {
        allItems.clear();
        allItems.addAll(data);
        super.setItems(data);
    }

    @Override
    protected void onSetItems() {
        if (!TextUtils.isEmpty(query)) {
            items.clear();
            items.addAll(allItems.stream()
                    .filter(address -> address.filter(query))
                    .collect(Collectors.toList()));
        }
        super.onSetItems();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                query = charSequence.toString();
                List<M> filterEntities;
                if (TextUtils.isEmpty(query)) {
                    filterEntities = allItems;
                } else {
                    filterEntities = allItems.stream()
                            .filter(coin -> coin.filter(query))
                            .collect(Collectors.toList());
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filterEntities;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values == null) {
                    items = new ArrayList<>();
                } else {
                    items = (ArrayList<M>) filterResults.values;
                }
                notifyDataSetChanged();
            }
        };
    }
}
