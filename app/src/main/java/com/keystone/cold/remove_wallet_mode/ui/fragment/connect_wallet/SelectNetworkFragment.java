package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectNetworkBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.adapter.ClickableAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.ClickableItem;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.Constants;

import java.util.Arrays;

public class SelectNetworkFragment extends BaseFragment<FragmentSelectNetworkBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_select_network;
    }

    @Override
    protected void init(View view) {
        ClickableAdapter.ClickableCallback callback = item -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, requireArguments().getString(BundleKeys.WALLET_ID_KEY));
            bundle.putString(BundleKeys.COIN_ID_KEY, item.getId());
            navigate(R.id.action_selectNetworkFragment_to_selectOneAddressFragment, bundle);
        };
        ClickableAdapter clickableAdapter = new ClickableAdapter(mActivity, callback);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.networks.setAdapter(clickableAdapter);
        clickableAdapter.setItems(Arrays.asList(
                new ClickableItem(Coins.DOT.coinId(), Coins.DOT.coinName(), R.drawable.coin_dot),
                new ClickableItem(Coins.KSM.coinId(), Coins.KSM.coinName(), R.drawable.coin_ksm))
        );
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
