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

package com.keystone.cold.ui.fragment.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.android.internal.app.LocalePicker;
import com.keystone.cold.R;
import com.keystone.cold.callables.ResetCallable;
import com.keystone.cold.databinding.DestructionModalBinding;
import com.keystone.cold.databinding.WebAuthResultBinding;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.util.DataCleaner;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.removeAllFingerprint;

public class WebAuthResultFragment extends SetupVaultBaseFragment<WebAuthResultBinding> {

    public static final String WEB_AUTH_DATA = "web_auth_data";

	private boolean isSetupVault;

	@Override
	protected int setView() {
		return R.layout.web_auth_result;
	}

	@Override
	protected void init(View view) {
		super.init(view);
		Bundle bundle = requireArguments();
		isSetupVault = bundle.getBoolean(IS_SETUP_VAULT);
		mBinding.setViewModel(viewModel);
		viewModel.calcAuthCode(bundle.getString(WEB_AUTH_DATA));
		mBinding.success.setOnClickListener(this::handleSuccess);
		mBinding.fail.setOnClickListener(this::handleFail);

		if (isSetupVault) {
			mBinding.toolbar.setVisibility(View.GONE);
			mBinding.divider.setVisibility(View.GONE);
		} else {
			mBinding.step.setVisibility(View.GONE);
			mBinding.toolbar.setNavigationOnClickListener(v ->navigate(R.id.action_auth_to_home));
		}
		MutableLiveData<String> webAuthResult = viewModel.getWebAuthCode();
		webAuthResult.observe(this, s -> {
			if (s != null) {
				mBinding.webAuthResult.setText(s);
				mBinding.progress.setVisibility(View.GONE);
				webAuthResult.removeObservers(this);
				webAuthResult.setValue(null);
			}
		});

	}

	private void handleFail(View view) {
		ModalDialog dialog = ModalDialog.newInstance();
		DestructionModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
				R.layout.destruction_modal,null,false);

		binding.cleanData.setOnClickListener(v -> {
			dialog.dismiss();
			cleanDataAndPowerOff(mActivity);
		});

		binding.retry.setOnClickListener(v -> {
			dialog.dismiss();
			if (isSetupVault) {
				navigate(R.id.action_webAuth_retry);
			} else {
				navigateUp();
			}
		});

		dialog.setBinding(binding);
		dialog.show(mActivity.getSupportFragmentManager(),"");
	}

	private void handleSuccess(View view) {
		if (isSetupVault) {
			Bundle bundle = new Bundle();
			bundle.putBoolean(IS_SETUP_VAULT, true);
			navigate(R.id.action_webAuthResultFragment_to_setPasswordFragment, bundle);
		} else {
			navigate(R.id.action_auth_to_home);
		}
	}

	private static void powerOff(Activity activity) {
		Intent i = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
		i.putExtra("android.intent.extra.KEY_CONFIRM", false);
		i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(i);
	}

	private void cleanDataAndPowerOff(AppCompatActivity activity) {
		ProgressModalDialog dialog = ProgressModalDialog.newInstance();
		dialog.show(Objects.requireNonNull(activity.getSupportFragmentManager()), "");
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				new ResetCallable().call();
				DataCleaner.cleanApplicationData(activity);
				removeAllFingerprint(activity);
				LocalePicker.updateLocale(Locale.ENGLISH);
			} catch (Exception ignored){
			}finally {
				DataCleaner.cleanApplicationData(activity);
				powerOff(activity);
			}
		});
	}

}
