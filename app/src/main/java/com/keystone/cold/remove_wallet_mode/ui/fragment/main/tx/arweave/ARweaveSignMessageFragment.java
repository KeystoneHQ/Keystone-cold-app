package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.ARweaveTxViewModel;
import com.keystone.cold.util.CharSetUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class ARweaveSignMessageFragment extends SignMessageFragment<ARweaveTxViewModel> {
    @Override
    protected void setupView() {
        mBinding.setCoinCode(Coins.AR.coinCode());
        mBinding.setCoinName(Coins.AR.coinName());
        LiveData<JSONObject> liveData = viewModel.parseMessage(requireArguments());
        liveData.observe(this, o -> {
            if (o == null) return;
            onMessageParsed(o);
            liveData.removeObservers(this);
        });
    }

    @Override
    protected void initViewModel() {

    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.AR.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }

    private void onMessageParsed(JSONObject jsonObject) {
        String message = null;
        try {
            message = jsonObject.getString("data");
            String fromAddress = jsonObject.getString("fromAddress");
            mBinding.address.setText(fromAddress);
            if (CharSetUtil.isUTF8Format(Hex.decode(message))) {
                String messageUtf8 = new String(Hex.decode(message), StandardCharsets.UTF_8);
                mBinding.message.setText(messageUtf8);
                mBinding.llMsgUtf8.setVisibility(View.VISIBLE);
            } else {
                mBinding.llMsgUtf8.setVisibility(View.GONE);
            }
            mBinding.rawMessage.setText(message);
        } catch (UnsupportedOperationException e) {
            mBinding.llMsgUtf8.setVisibility(View.GONE);
            mBinding.rawMessage.setText(message);
        } catch (JSONException e) {
            e.printStackTrace();
            handleParseException(e);
        }
    }
}
