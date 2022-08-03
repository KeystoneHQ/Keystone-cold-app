package com.keystone.cold.ui.fragment.main.near;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentNearTxDetailBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.near.model.NearTx;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.tx.NearTxViewModel;

public class NearTxDetailFragment extends BaseFragment<FragmentNearTxDetailBinding> {

    private NearTxViewModel viewModel;
    private TxEntity txEntity;
    private Fragment[] fragments;
    private Bundle bundle;

    @Override
    protected int setView() {
        return R.layout.fragment_near_tx_detail;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(this).get(NearTxViewModel.class);
        bundle = requireArguments();

        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                viewModel.parseNearTxEntity(txEntity);
            }
        });
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        initViewPager();
    }

    private void initViewPager() {
        String[] title = {getString(R.string.overview), getString(R.string.raw_data)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = NearFormattedRecordTxFragment.newInstance(bundle);
            fragments[1] = NearRawRecordTxFragment.newInstance(bundle);
        }

        mBinding.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewPager);
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
