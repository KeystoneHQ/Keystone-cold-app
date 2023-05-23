package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.sui;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SuiTxViewModel;


public class SuiSignMessageFragment extends SignMessageFragment<SuiTxViewModel> {


    @Override
    protected String getCoinName() {
        return Coins.SUI.coinName();
    }

    @Override
    protected String getCoinCode() {
        return Coins.SUI.coinCode();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SuiTxViewModel.class);
    }


}
