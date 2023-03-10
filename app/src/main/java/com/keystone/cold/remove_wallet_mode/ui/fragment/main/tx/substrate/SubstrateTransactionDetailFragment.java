package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSubstrateTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.PolkadotErrorDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubstrateTransactionDetailFragment extends BaseFragment<FragmentSubstrateTxBinding> {
    private final MutableLiveData<SubstrateTransaction> transaction;

    public SubstrateTransactionDetailFragment(MutableLiveData<SubstrateTransaction> transaction) {
        this.transaction = transaction;
    }

    public static SubstrateTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<SubstrateTransaction> transaction) {
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
        transaction.observe(this, (v) -> {
            if (v != null) {
                UpdateUI(v);
            }
        });
    }

    private void UpdateUI(SubstrateTransaction transaction) {
        try {
            JSONObject object = transaction.getParsedTransaction();
            String type = object.getString("transaction_type");
            JSONArray content = object.getJSONArray("content");
            mBinding.checkInfo.head.setVisibility(View.GONE);
            switch (type) {
                case "Sign": {
                    mBinding.txView.txDetail.updateUI(content);
                    mBinding.checkInfo.head.setVisibility(View.VISIBLE);
                    mBinding.checkInfo.checkInfoContent.setText(R.string.check_info_dot);
                    mBinding.setCoinCode(transaction.getCoinCode());
                    mBinding.setCheckInfoTitle(Coins.coinNameFromCoinCode(transaction.getCoinCode()));
                    break;
                }
                case "Stub": {
                    mBinding.txView.txDetail.updateUI(content);
                    break;
                }
                case "Read": {
                    mBinding.root.setVisibility(View.GONE);
                    PolkadotErrorDialog.show(mActivity, getString(R.string.notice), getString(R.string.decline), content, this::navigateUp);
                    break;
                }
                default: {
                    ModalDialog.showCommonModal(mActivity, "Warning", "Action " + type + " is not supported currently", "OK", null);
                    navigateUp();
                }
            }
            if (transaction.getSignedHex() != null) {
                mBinding.qrcodeContainer.setVisibility(View.VISIBLE);
                mBinding.qrcode.qrcode.disableMultipart();
                mBinding.qrcode.qrcode.setData(transaction.getSignedHex());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
