package com.keystone.cold.ui.fragment.main.tutorials;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;

import com.keystone.cold.databinding.FragmentTutorialsBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.ArrayList;
import java.util.List;

public class TutorialsFragment extends BaseFragment<FragmentTutorialsBinding> {

    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.fragment_tutorials;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        List<TutorialItem> tutorialItems = initTutorialsData();
        mBinding.tutorialList.setItemData(tutorialItems);
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
        switch (watchWallet) {
            case METAMASK:
                id = R.array.tutorials_name_web3;
                break;
            case SOLANA:
                id = R.array.tutorials_name_sol;
                break;
            case POLKADOT_JS:
                id = R.array.tutorials_name_polkadot;
                break;
            case XRP_TOOLKIT:
                id = R.array.tutorials_name_xrp;
                break;

        }
        return id;
    }

    private int getTutorialLinkId() {
        int id = R.array.tutorials_link_web3;
        switch (watchWallet) {
            case METAMASK:
                id = R.array.tutorials_link_web3;
                break;
            case SOLANA:
                id = R.array.tutorials_link_sol;
                break;
            case POLKADOT_JS:
                id = R.array.tutorials_link_polkadot;
                break;
            case XRP_TOOLKIT:
                id = R.array.tutorials_link_xrp;
                break;
        }
        return id;
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
