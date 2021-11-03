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

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static com.keystone.cold.ui.fragment.setting.SystemPreferenceFragment.SETTING_LANGUAGE;

import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.PrivacyPolicyBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LicenseFragment extends BaseFragment<PrivacyPolicyBinding> {

    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    private String prefix;

    @Override
    protected int setView() {
        return R.layout.privacy_policy;
    }

    @Override
    protected void init(View view) {
        requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(view1 -> navigateUp());
        mBinding.toolbarTitle.setText(getArguments().getString(KEY_TITLE));
        prefix = Utilities.getPrefs(MainApplication.getApplication())
                .getString(SETTING_LANGUAGE, "zh_rCN");
        if (!prefix.equals("zh_rCN")) {
            prefix = "en";
        }
        AppExecutors.getInstance().diskIO().execute(() -> {
            String text = readFromAssets(prefix + "_" + getArguments().getString(KEY_URL));
            AppExecutors.getInstance()
                    .mainThread()
                    .execute(() -> mBinding.content.setText(Html.fromHtml(text, FROM_HTML_MODE_LEGACY)));
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String readFromAssets(String url) {
        try {
            InputStream is = mActivity.getAssets().open(url);
            String text = readText(is);
            return text.replace("\r\n", "<br />")
                    .replace("\n", "<br />");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String readText(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder buffer = new StringBuilder();
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
