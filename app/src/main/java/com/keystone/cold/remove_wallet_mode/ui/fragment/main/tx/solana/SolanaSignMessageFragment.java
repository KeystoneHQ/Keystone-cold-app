package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SolanaTxViewModel;

public class SolanaSignMessageFragment extends SignMessageFragment<SolanaTxViewModel> {

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SolanaTxViewModel.class);
    }

    @Override
    protected String getCoinName() {
        return Coins.SOL.coinName();
    }

    @Override
    protected String getCoinCode() {
        return Coins.SOL.coinCode();
    }
}
