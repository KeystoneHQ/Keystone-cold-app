package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.CharSetUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class AptosSignMessageFragment extends SignMessageFragment<AptosTxViewModel> {

    @Override
    protected void setupView() {
        mBinding.setCoinCode(Coins.APTOS.coinCode());
        mBinding.setCoinName(Coins.APTOS.coinName());
        LiveData<JSONObject> liveData = viewModel.parseMessage(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AptosTxViewModel.class);
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            String message = null;
            try {
                message = jsonObject.getString("data");
                String fromAddress = jsonObject.getString("fromAddress");
                mBinding.address.setText(fromAddress);
                if (CharSetUtil.isUTF8Format(Hex.decode(message))) {
                    String messageUtf8 = new String(Hex.decode(message), StandardCharsets.UTF_8);
                    mBinding.message.setText(messageUtf8);
                } else {
                    mBinding.llMsgUtf8.setVisibility(View.GONE);
                }
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(this);
            } catch (UnsupportedOperationException e) {
                mBinding.llMsgUtf8.setVisibility(View.GONE);
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(this);
            } catch (JSONException e) {
                e.printStackTrace();
                handleParseException(e);
            }
        }
    }


    private void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.invalid_data),
                    getString(R.string.incorrect_tx_data),
                    getString(R.string.confirm),
                    null);
            popBackStack(R.id.myAssetsFragment, false);
        }
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.APTOS.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
