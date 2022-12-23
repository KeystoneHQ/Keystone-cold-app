package com.keystone.cold.ui.fragment.main.psbt;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.PsbtConfirmFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.SignState;
import com.keystone.cold.viewmodel.tx.psbt.PSBT;
import com.keystone.cold.viewmodel.tx.psbt.PSBTViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PSBTConfirmFragment extends BaseFragment<PsbtConfirmFragmentBinding> {
    PSBTViewModel psbtViewModel;
    PSBT psbt;
    private SigningDialog signingDialog;

    @Override
    protected int setView() {
        return R.layout.psbt_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();

        String psbtB64 = bundle.getString("psbt");

        psbtViewModel = ViewModelProviders.of(this).get(PSBTViewModel.class);
        JSONObject parsedPSBT;
        List<JSONObject> inputs = new ArrayList<>();
        List<JSONObject> outputs = new ArrayList<>();
        String fee = null;
        try {
            String myMasterFingerprint = new GetMasterFingerprintCallable().call();
            psbt = psbtViewModel.parsePsbtBase64(psbtB64, myMasterFingerprint);
            parsedPSBT = psbt.generateParsedMessage();
            JSONArray pinputs = parsedPSBT.getJSONArray("inputs");
            JSONArray poutputs = parsedPSBT.getJSONArray("outputs");
            fee = parsedPSBT.getString("fee");
            int length = pinputs.length();
            for (int i = 0; i < length; i++) {
                JSONObject o = pinputs.getJSONObject(i);
                inputs.add(o);
            }

            length = poutputs.length();
            for (int i = 0; i < length; i++) {
                JSONObject o = poutputs.getJSONObject(i);
                outputs.add(o);
            }
        } catch (InvalidTransactionException | JSONException e) {
            this.alert("Invalid Transaction", e.getMessage(), this::navigateUp);
        }

        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        mBinding.txDetail.qr.setVisibility(View.GONE);

        PSBTInputAdapter inputAdapter = new PSBTInputAdapter(mActivity);

        inputAdapter.setItems(inputs);
        mBinding.txDetail.fromList.setAdapter(inputAdapter);

        PSBTOutputAdapter outputAdapter = new PSBTOutputAdapter(mActivity);
        outputAdapter.setItems(outputs);
        mBinding.txDetail.toList.setAdapter(outputAdapter);

        mBinding.txDetail.fee.setText(fee);

        mBinding.sign.setOnClickListener(v -> {
            handleSign(psbt);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    private void handleSign(PSBT psbt) {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    psbtViewModel.setToken(token);
                    MutableLiveData<SignState> signState = psbtViewModel.handleSignPSBT(psbt);
                    subscribeSignState(signState);
                }, forgetPassword);
    }

    private void onSignSuccess(String txId) {
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        navigate(R.id.action_to_psbtBroadcastFragment, data);
    }

    private void subscribeSignState(MutableLiveData<SignState> signState) {
        signState.observe(this, s -> {
            if (PSBTViewModel.STATE_SIGNING.equals(s.getStatus())) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (PSBTViewModel.STATE_SIGN_SUCCESS.equals(s.getStatus())) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess(s.getTxId());
                }, 500);
            } else if (PSBTViewModel.STATE_SIGN_FAIL.equals(s.getStatus())) {
                if (signingDialog == null) {
                    signingDialog = SigningDialog.newInstance();
                    signingDialog.show(mActivity.getSupportFragmentManager(), "");
                }
                new Handler().postDelayed(() -> signingDialog.setState(SigningDialog.STATE_FAIL), 1000);
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    signState.removeObservers(this);
                }, 2000);
            }
        });
    }

}
