package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SolanaTxViewModel;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class SolanaSignMessageFragment extends SignMessageFragment<SolanaTxViewModel> {

    private String rawMessage;

    @Override
    protected void setupView() {
        rawMessage = requireArguments().getString(BundleKeys.SIGN_DATA_KEY);
        mBinding.setCoinCode(Coins.SOL.coinCode());
        mBinding.setCoinName(Coins.SOL.coinName());
        LiveData<JSONObject> liveData = viewModel.parseMessage(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SolanaTxViewModel.class);
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                String message = jsonObject.getString("data");
                String fromAddress = jsonObject.getString("fromAddress");
                mBinding.address.setText(fromAddress);
                String messageUtf8 = new String(Hex.decode(message), StandardCharsets.UTF_8);
                mBinding.message.setText(messageUtf8);
                mBinding.llMsgUtf8.setVisibility(View.VISIBLE);
                mBinding.rawMessage.setText(message);
            } catch (Exception e) {
                mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
                mBinding.rawMessage.setText(rawMessage);
                handleParseException(e);
            }
        } else {
            mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
            mBinding.rawMessage.setText(rawMessage);
        }
        liveData.removeObservers(this);
    }


    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.SOL.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
