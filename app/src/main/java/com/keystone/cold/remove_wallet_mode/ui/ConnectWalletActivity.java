package com.keystone.cold.remove_wallet_mode.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ActivityConnectWalletBinding;
import com.keystone.cold.ui.common.FullScreenActivity;

public class ConnectWalletActivity extends FullScreenActivity {

    private ActivityConnectWalletBinding mBinding;
    private NavController mNavController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_connect_wallet);
        initViews();
        initNavController();
    }

    private void initNavController() {
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    private void initViews() {
    }
}
