package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentTutorialBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.modal.QRDialog;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.tutorials.TutorialItem;

import java.util.ArrayList;
import java.util.List;

public class TutorialsFragment extends BaseFragment<FragmentTutorialBinding> {

    private Wallet wallet;

    @Override
    protected int setView() {
        return R.layout.fragment_tutorial;
    }

    @Override
    protected void init(View view) {
        wallet = Wallet.getWalletById(requireArguments().getString(BundleKeys.WALLET_ID_KEY));
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        List<TutorialItem> tutorialItems = initTutorialsData();
        mBinding.tutorialList.setItemData(tutorialItems);
        mBinding.tutorialList.setOnItemClickListener(tutorialItem -> QRDialog.show(mActivity,tutorialItem.getLink(), tutorialItem.getName()));
    }

    private List<TutorialItem> initTutorialsData() {
        List<TutorialItem> tutorialItems = new ArrayList<>();
        String[] nameArray = getResources().getStringArray(getTutorialNameId());
        String[] linkArray = getResources().getStringArray(getTutorialLinkId());
        if (nameArray.length == linkArray.length) {
            for (int i = 0; i < nameArray.length; i++) {
                TutorialItem tutorialItem = new TutorialItem();
                tutorialItem.setName(nameArray[i]);
                tutorialItem.setLink(linkArray[i]);
                tutorialItems.add(tutorialItem);
            }
        }
        return tutorialItems;
    }


    private int getTutorialNameId() {
        int id = R.array.tutorials_name_web3;
        switch (wallet) {
            case METAMASK:
                id = R.array.tutorials_name_web3;
                break;
            case SOLFLARE:
                id = R.array.tutorials_name_sol;
                break;
            case POLKADOTJS:
                id = R.array.tutorials_name_polkadot;
                break;
            case XRPTOOLKIT:
                id = R.array.tutorials_name_xrp;
                break;
            case SENDER:
                id = R.array.tutorials_name_sender;
                break;
            case FEWCHA:
                id = R.array.tutorials_name_aptos;
                break;
            case SUIET:
                id = R.array.tutorials_name_suiet;
                break;
            case CORE:
                id = R.array.tutorials_name_core_wallet;
                break;
            case BLUEWALLET:
                id = R.array.tutorials_name_blue_wallet;
                break;
            case BITKEEP:
                id = R.array.tutorials_name_bit_keep;
                break;
            case KEPLR:
                id = R.array.tutorials_name_cosmos;
                break;
            case ARCONNECT:
                id = R.array.tutorials_name_arconnect;
                break;
            case KEYSTONE:
                id = R.array.tutorials_name_keystone;
                break;
            case PETRA:
                id = R.array.tutorials_name_petra;
                break;
            case ZAPPER:
            case YEARN:
            case SUSHISWAP:
                id = R.array.tutorials_name_defi;
                break;
            case SUBWALLET:
                id = R.array.tutorials_name_sub_wallet;
                break;
            case SAFE:
                id = R.array.tutorials_name_safe;
                break;
            case BLOCKWALLET:
                id = R.array.tutorials_name_blockwallet;
                break;
            case OKX:
                id = R.array.tutorials_name_okx;
                break;
        }
        return id;
    }

    private int getTutorialLinkId() {
        int id = R.array.tutorials_link_web3;
        switch (wallet) {
            case METAMASK:
                id = R.array.tutorials_link_web3;
                break;
            case SOLFLARE:
                id = R.array.tutorials_link_sol;
                break;
            case POLKADOTJS:
                id = R.array.tutorials_link_polkadot;
                break;
            case XRPTOOLKIT:
                id = R.array.tutorials_link_xrp;
                break;
            case SENDER:
                id = R.array.tutorials_link_sender;
                break;
            case FEWCHA:
                id = R.array.tutorials_link_aptos;
                break;
            case SUIET:
                id = R.array.tutorials_link_suiet;
                break;
            case CORE:
                id = R.array.tutorials_link_core_wallet;
                break;
            case BLUEWALLET:
                id = R.array.tutorials_link_blue_wallet;
                break;
            case BITKEEP:
                id = R.array.tutorials_link_bit_keep;
                break;
            case KEPLR:
                id = R.array.tutorials_link_cosmos;
                break;
            case ARCONNECT:
                id = R.array.tutorials_link_arconnect;
                break;
            case KEYSTONE:
                id = R.array.tutorials_link_keystone;
                break;
            case PETRA:
                id = R.array.tutorials_link_petra;
                break;
            case ZAPPER:
            case YEARN:
            case SUSHISWAP:
                id = R.array.tutorials_link_defi;
                break;
            case SUBWALLET:
                id = R.array.tutorials_link_sub_wallet;
                break;
            case SAFE:
                id = R.array.tutorials_link_safe;
                break;
            case BLOCKWALLET:
                id = R.array.tutorials_link_blockwallet;
                break;
            case OKX:
                id = R.array.tutorials_link_okx;
                break;
        }
        return id;
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
