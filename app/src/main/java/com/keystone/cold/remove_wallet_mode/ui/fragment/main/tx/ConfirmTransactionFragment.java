package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentConfirmTransactionBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public abstract class ConfirmTransactionFragment extends BaseFragment<FragmentConfirmTransactionBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_confirm_transaction;
    }

    @Override
    protected void init(View view) {
        setupView();
        setupViewPager();
    }

    protected abstract TabLayoutConfig[] getTabLayouts();

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    protected abstract void setupView();

    private void setupViewPager() {
        TabLayoutConfig[] configs = getTabLayouts();
        mBinding.transaction.viewPager.setOffscreenPageLimit(configs.length);
        mBinding.transaction.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return configs[position].fragment;
            }

            @Override
            public int getCount() {
                return configs.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return configs[position].name;
            }
        });
        mBinding.transaction.tab.setupWithViewPager(mBinding.transaction.viewPager);
    }

    protected static class TabLayoutConfig {
        private final String name;
        private final Fragment fragment;

        public TabLayoutConfig(String name, Fragment fragment) {
            this.name = name;
            this.fragment = fragment;
        }

        public String getName() {
            return name;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }
}
