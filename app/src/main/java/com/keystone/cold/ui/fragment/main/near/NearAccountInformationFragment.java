package com.keystone.cold.ui.fragment.main.near;

import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentNearAccountInfoBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.Objects;

public class NearAccountInformationFragment extends BaseFragment<FragmentNearAccountInfoBinding> {


    @Override
    protected int setView() {
        return R.layout.fragment_near_account_info;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        String coinCode = data.getString(KEY_COIN_CODE);
        mBinding.setDisplayName(coinCode);
        mBinding.setCoinCode(coinCode);
        mBinding.setAddress(data.getString(KEY_ADDRESS));
        mBinding.setAddressName(data.getString(KEY_ADDRESS_NAME));
        mBinding.setPath(data.getString(KEY_ADDRESS_PATH));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
