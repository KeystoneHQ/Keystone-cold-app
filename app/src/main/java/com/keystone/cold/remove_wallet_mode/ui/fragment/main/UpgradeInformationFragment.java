package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentUpgradeInfoBinding;
import com.keystone.cold.remove_wallet_mode.ui.modal.QRDialog;
import com.keystone.cold.ui.fragment.BaseFragment;

public class UpgradeInformationFragment extends BaseFragment<FragmentUpgradeInfoBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_upgrade_info;
    }

    @Override
    protected void init(View view) {
        mBinding.btLearnMore.setOnClickListener(v -> QRDialog.show(mActivity, getString(R.string.firmware_information_link), getString(R.string.firmware_information_reason)));
        mBinding.btOk.setOnClickListener(v -> navigate(R.id.action_to_myAssetsFragment));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
