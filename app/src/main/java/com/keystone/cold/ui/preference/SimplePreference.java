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

package com.keystone.cold.ui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.keystone.cold.R;

public class SimplePreference extends Preference {

    private String remind;
    private PreferenceViewHolder holder;

    public SimplePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.setting_item);
        setWidgetLayoutResource(R.layout.arrow);
    }

    public SimplePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SimplePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePreference(Context context) {
        this(context, null);
    }

    public void setRemindText(String s) {
        remind = s;
        notifyChanged();
    }

    @Override
    public void setSummary(int summaryResId) {
        super.setSummary(summaryResId);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView remind = (TextView) holder.findViewById(R.id.remind);
        remind.setText(this.remind);
        this.holder = holder;
    }


    public PreferenceViewHolder getViewHolder() {
        return holder;
    }
}
