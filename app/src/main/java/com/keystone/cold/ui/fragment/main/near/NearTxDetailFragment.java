package com.keystone.cold.ui.fragment.main.near;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

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


    @Override
    protected int setView() {
        return R.layout.fragment_near_tx_detail;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(getParentFragment()).get(NearTxViewModel.class);
        Bundle bundle = requireArguments();

        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                updateUI();
            }
        });
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
    }

    private void updateUI() {
        String ur = txEntity.getSignedHex();
        NearTx nearTx = viewModel.parseNearTxEntity(txEntity);

        mBinding.network.setText(nearTx.getNetWork());
        mBinding.from.setText(nearTx.getSignerId());
        mBinding.to.setText(nearTx.getReceiverId());
        mBinding.actions.setData(nearTx);

        mBinding.qrcode.qrcode.setData(ur);
        mBinding.qr.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
