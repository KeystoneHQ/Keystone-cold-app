package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSubstrateTxBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.PolkadotViewModel;
import com.keystone.cold.viewmodel.tx.PolkadotJsTxConfirmViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubstrateTransactionDetailFragment extends BaseFragment<FragmentSubstrateTxBinding> {
    private MutableLiveData<JSONObject> transaction;

    private PolkadotJsTxConfirmViewModel viewModel;
    private PolkadotViewModel polkadotViewModel;

    public SubstrateTransactionDetailFragment(MutableLiveData<JSONObject> transaction) {
        this.transaction = transaction;
    }

    public static SubstrateTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<JSONObject> transaction) {
        SubstrateTransactionDetailFragment fragment = new SubstrateTransactionDetailFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_substrate_tx;
    }

    @Override
    protected void init(View view) {
        polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
        viewModel = ViewModelProviders.of(this).get(PolkadotJsTxConfirmViewModel.class);
        transaction.observe(this, (v) -> {
            if (v != null) {
                UpdateUI(v);
            }
        });
    }

    private void UpdateUI(JSONObject object) {
        try {
            String type = object.getString("transaction_type");
            JSONArray content = object.getJSONArray("content");
            mBinding.checkInfo.head.setVisibility(View.GONE);
            switch (type) {
                case "Sign": {
                    mBinding.txDetail.updateUI(content);
                    mBinding.checkInfo.head.setVisibility(View.VISIBLE);
                    TxEntity tx = viewModel.generateAndPostSubstrateTxV2(object, data);
                    mBinding.setCoinCode(tx.getCoinCode());
                    mBinding.setCheckInfoTitle(Coins.coinNameFromCoinCode(tx.getCoinCode()));
                    break;
                }
                case "Stub": {
                    mBinding.txDetail.updateUI(content);
                    break;
                }
                // "Read" has already handled in scanner
                default: {
                    ModalDialog.showCommonModal(mActivity, "Warning", "Action " + type + " is not supported currently", "OK", null);
                    navigateUp();
                }
            }
        }catch (PolkadotViewModel.PolkadotException | JSONException e) {
            e.printStackTrace();
        } catch (InvalidAccountException e) {
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.account_not_match),
                    getString(R.string.account_not_match_detail),
                    getString(R.string.confirm),
                    this::navigateUp);
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
