package com.keystone.cold.ui.fragment.main.solana;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolTxDetailBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.solana.model.SolTxData;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class SolTxDetailFragment extends BaseFragment<FragmentSolTxDetailBinding> {

    private SolTxViewModel viewModel;
    private TxEntity txEntity;
    private Fragment[] fragments;
    private SolTxData solTxData;

    @Override
    protected int setView() {
        return R.layout.fragment_sol_tx_detail;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(this).get(SolTxViewModel.class);
        Bundle bundle = requireArguments();

        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                solTxData = viewModel.parseSolTxEntity(txEntity);
                updateUI();
            }
        });
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
    }

    private void initViewPager() {
        String[] title = {getString(R.string.overview), getString(R.string.details), getString(R.string.raw), getString(R.string.qr)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = SolInstructionFragment.newInstance(true, false);
            fragments[1] = SolInstructionFragment.newInstance(false, false);
            fragments[2] = SolInstructionFragment.newInstance(false, true);
            fragments[3] = SolSignResultFragment.newInstance(solTxData);
        }
        mBinding.viewPager.setOffscreenPageLimit(3);
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

    private void updateUI() {
        initViewPager();
        String parsedMessage = viewModel.parseTxData(solTxData.getRawMessage());
        if (parsedMessage != null) {
            try {
                JSONObject jsonObject = new JSONObject(parsedMessage);
                viewModel.setParseMessageJsonLiveData(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
