package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentChangeDerivationPathBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.helper.SyncMode;
import com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.config.WalletConfig;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.ChangePathViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.WalletViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.List;


public class ChangeDerivationPathFragment extends BaseFragment<FragmentChangeDerivationPathBinding> {
    private String coinId;
    private String walletId;
    private String selectCode;
    private ChangePathViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.fragment_change_derivation_path;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        coinId = data.getString(BundleKeys.COIN_ID_KEY);
        walletId = data.getString(BundleKeys.WALLET_ID_KEY);
        if (coinId.equals(Coins.BTC.coinId())) {
            mBinding.toolbarTitle.setText(R.string.change_address_type);
            String hintText = getString(R.string.changePathText, "Bitcoin");
            mBinding.tvTittleHint.setText(hintText);
        } else {
            String hintText = getString(R.string.changePathText, Coins.coinNameOfCoinId(coinId));
            mBinding.tvTittleHint.setText(hintText);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.ivConfirm.setOnClickListener(v -> save());
        mBinding.pathPatternView.setOnItemClick(code -> selectCode = code);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(ChangePathViewModel.class);
        subscribeUI(viewModel.getPathPattern(coinId));
    }

    private void subscribeUI(LiveData<List<PathPatternItem>> pathPatternItemsLiveData) {
        pathPatternItemsLiveData.observe(this, pathPatternItems -> {
            mBinding.pathPatternView.setData(pathPatternItems);
            pathPatternItemsLiveData.removeObservers(this);
        });
    }

    private void save() {
        viewModel.save(coinId, selectCode);
        if (!TextUtils.isEmpty(walletId)) {
            stepSync();
        } else {
            navigateUp();
        }
    }

    private void stepSync() {
        WalletViewModel walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        LiveData<SyncMode> stepMode = walletViewModel.determineSyncMode(walletId);
        stepMode.observe(this, mode -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
            WalletConfig config = WalletConfig.getConfigByWalletId(walletId);
            bundle.putString(BundleKeys.COIN_ID_KEY, config.getCoinId());
            switch (mode) {
                case DIRECT:
                    popBackStack(R.id.walletListFragment, false);
                    navigate(R.id.action_to_syncFragment, bundle);
                    break;
                case SELECT_ADDRESS:
                    popBackStack(R.id.walletListFragment, false);
                    navigate(R.id.action_to_selectAddressFragment, bundle);
                    break;
                case SELECT_ONE_ADDRESS:
                    popBackStack(R.id.walletListFragment, false);
                    navigate(R.id.action_to_selectOneAddressFragment, bundle);
                    break;
                case SELECT_COINS:
                    if (walletId.equals(Wallet.BITKEEP.getWalletId())) {
                        navigateUp();
                    } else {
                        popBackStack(R.id.walletListFragment, false);
                        navigate(R.id.action_walletListFragment_to_selectNetworksFragment, bundle);
                    }
                    break;
            }
            stepMode.removeObservers(this);
        });
    }
}
