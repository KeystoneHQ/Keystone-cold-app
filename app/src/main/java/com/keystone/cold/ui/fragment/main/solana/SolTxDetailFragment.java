package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

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
                updateUI();
            }
        });
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
    }

    private void updateUI() {

        SolTxData solTxData = viewModel.parseSolTxEntity(txEntity);
        try {
            mBinding.txMessage.setText(new JSONObject(solTxData.getParsedMessage()).toString(2));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        mBinding.qrcode.qrcode.setData(solTxData.getSignatureUR());
        mBinding.qr.setVisibility(View.VISIBLE);
    }



    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
