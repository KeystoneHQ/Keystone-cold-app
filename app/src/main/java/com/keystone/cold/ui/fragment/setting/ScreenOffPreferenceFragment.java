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

package com.keystone.cold.ui.fragment.setting;

import com.keystone.cold.R;
import com.keystone.cold.setting.PowerHelper;

import static com.keystone.cold.ui.fragment.setting.SystemPreferenceFragment.SETTING_SCREEN_OFF_TIME;

public class ScreenOffPreferenceFragment extends ListPreferenceFragment {

    @Override
    protected int getEntries() {
        return R.array.screen_off_entries;
    }

    @Override
    protected int getValues() {
        return R.array.screen_off_values;
    }

    @Override
    protected String getKey() {
        return SETTING_SCREEN_OFF_TIME;
    }

    @Override
    protected String defaultValue() {
        return String.valueOf(PowerHelper.getScreenOffTimeout(mActivity));
    }

    @Override
    public void onSelect(int position) {
        value = values[position].toString();
        prefs.edit().putString(SETTING_SCREEN_OFF_TIME, value).apply();
        adapter.notifyDataSetChanged();
        PowerHelper.setScreenOffTimeout(mActivity, Integer.valueOf(value));
    }
}

