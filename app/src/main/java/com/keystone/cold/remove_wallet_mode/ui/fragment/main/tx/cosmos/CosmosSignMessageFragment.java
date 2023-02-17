package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CosmosTxViewModel;


public class CosmosSignMessageFragment extends SignMessageFragment<CosmosTxViewModel> {

    @Override
    protected String getCoinName() {
        return "Cosmos Ecosystem";
    }

    @Override
    protected String getCoinCode() {
        return "cosmos_default";
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(CosmosTxViewModel.class);
    }

}
