package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;


public class AptosSignMessageFragment extends SignMessageFragment<AptosTxViewModel> {


    @Override
    protected String getCoinName() {
        return Coins.APTOS.coinName();
    }

    @Override
    protected String getCoinCode() {
        return Coins.APTOS.coinCode();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AptosTxViewModel.class);
    }


}
