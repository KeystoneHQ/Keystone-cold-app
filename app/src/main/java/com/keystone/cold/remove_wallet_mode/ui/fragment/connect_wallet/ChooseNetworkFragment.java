package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentChooseNetworkBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.adapter.ClickableAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.ClickableItem;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseNetworkFragment extends BaseFragment<FragmentChooseNetworkBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_choose_network;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);

        ClickableAdapter.ClickableCallback callback = item -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
            bundle.putString(BundleKeys.COIN_ID_KEY, item.getId());
            navigate(R.id.action_chooseNetworkFragment_to_selectOneAddressFragment, bundle);
        };
        ClickableAdapter clickableAdapter = new ClickableAdapter(mActivity, callback);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.networks.setAdapter(clickableAdapter);
        clickableAdapter.setItems(getNetworkList(walletId));
    }

    private List<ClickableItem> getNetworkList(String walletId) {
        Wallet wallet = Wallet.getWalletById(walletId);
        List<ClickableItem> list;
        if (wallet == Wallet.POLKADOTJS || wallet == Wallet.SUBWALLET) {
            list = Arrays.asList(
                    new ClickableItem(Coins.DOT.coinId(), Coins.DOT.coinName(), R.drawable.coin_dot),
                    new ClickableItem(Coins.KSM.coinId(), Coins.KSM.coinName(), R.drawable.coin_ksm)
            );
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
