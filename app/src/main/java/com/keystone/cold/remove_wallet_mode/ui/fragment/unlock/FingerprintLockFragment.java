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

package com.keystone.cold.remove_wallet_mode.ui.fragment.unlock;

import static com.keystone.cold.ui.fragment.Constants.IS_FORCE;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentFingerprintLockBinding;
import com.keystone.cold.remove_wallet_mode.ui.UnlockActivity;
import com.keystone.cold.ui.fragment.BaseFragment;

public class FingerprintLockFragment extends BaseFragment<FragmentFingerprintLockBinding> {

    public static final String TAG = "FingerprintLockFragment";
    public int attemptTimes;

    private NFTAwareToolbarFragment nftAwareToolbarFragment;

    private final View.OnClickListener gotoPasswordUnlock =
            v -> {
                Bundle data = new Bundle();
                data.putBoolean(IS_FORCE, false);
                navigate(R.id.action_fingerprint_to_passwordLockFragment, data);
            };

    @Override
    protected int setView() {
        return R.layout.fragment_fingerprint_lock;
    }

    @Override
    protected void init(View view) {
        nftAwareToolbarFragment = new NFTAwareToolbarFragment(false, null, R.dimen.dp_188, R.dimen.dp_64);
        getChildFragmentManager().beginTransaction().replace(R.id.toolbar_container, nftAwareToolbarFragment).commit();
        mBinding.switchToPassword.setOnClickListener(gotoPasswordUnlock);
        attemptTimes = Utilities.getPatternRetryTimes(mActivity);
        ((UnlockActivity) mActivity).setStatusHint(mBinding.verifyHint);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if(nftAwareToolbarFragment.isShowNFT()) {
            mBinding.hint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((UnlockActivity) mActivity).startIdentify();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((UnlockActivity) mActivity).cancelIdentify();
    }
}
