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

package com.keystone.cold.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.AboutFragmentBinding;
import com.keystone.cold.logging.FileLogger;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.viewmodel.AboutViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AboutFragment extends BaseFragment<AboutFragmentBinding> {

    public static final String TAG = "AboutFragment";

    @Override
    protected int setView() {
        return R.layout.about_fragment;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        mBinding.toolbar.setTitle("");
        mBinding.logo.setOnClickListener(new ExportLogHandler(mActivity,Executors.newSingleThreadExecutor()));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mBinding.setViewModel(ViewModelProviders.of(this).get(AboutViewModel.class));
    }

    public static class ExportLogHandler implements View.OnClickListener {

        private Activity activity;
        ExecutorService executor;

        final int COUNTS = 6;
        final long DURATION = 3000L;
        long[] mHits = new long[COUNTS];
        boolean isExporting;

        public ExportLogHandler(Activity activity, ExecutorService executor) {
            this.activity = activity;
            this.executor = executor;
        }

        @Override
        public void onClick(View v) {
            if (isExporting) {
                return;
            }
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                executor.execute(() -> {
                    isExporting = true;
                    String message;
                    if (FileLogger.exportLogfiles(activity)) {
                        message = "export log success";
                    } else {
                        message = "export log failed";
                    }
                    isExporting = false;
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
                });
            }
        }

    }
}
