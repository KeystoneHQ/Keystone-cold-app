package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.CosmosTxViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class CosmosSignMessageFragment extends SignMessageFragment<CosmosTxViewModel> {

    @Override
    protected void setupView() {
        mBinding.setCoinCode("cosmos_default");
        mBinding.setCoinName("Cosmos Ecosystem");
        LiveData<JSONObject> liveData = viewModel.parseMessage(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            String message = null;
            try {
                message = jsonObject.getString("data");
                String fromAddress = jsonObject.getString("signer");
                mBinding.address.setText(fromAddress);
                mBinding.rawMessage.setText(message);
                mBinding.llMsgUtf8.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
                handleParseException(e);
            }
        }
        liveData.removeObservers(this);
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(CosmosTxViewModel.class);
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, "cosmos_default");
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
