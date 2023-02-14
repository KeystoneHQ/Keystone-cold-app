package com.keystone.cold.ui.fragment.main.arweave;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_SIGNATURE_UR;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.ArweaveTxConfirmBinding;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.ArweaveTxViewModel;
import com.keystone.cold.viewmodel.tx.SignState;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ArweaveTxConfirmFragment extends BaseFragment<ArweaveTxConfirmBinding> {
    public static final String KEY_SALT_LEN = "salt_len";
    private ArweaveTxViewModel viewModel;
    private Fragment[] fragments;

    private String rawTx;
    private ArweaveTransaction parsedTx;

    private SigningDialog signingDialog;

    private String requestId;

    private DataRepository mRepository;

    @Override
    protected int setView() {
        return R.layout.arweave_tx_confirm;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        String signData = bundle.getString(SIGN_DATA);
        requestId = bundle.getString(REQUEST_ID);
        int saltLen = bundle.getInt(KEY_SALT_LEN);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mRepository = ((MainApplication) mActivity.getApplication()).getRepository();

        viewModel = ViewModelProviders.of(this).get(ArweaveTxViewModel.class);
        viewModel.setRequestId(requestId);
        viewModel.parseTransaction(signData).observe(this, (v) -> {
            if (v == null) return;
            try {
                if (v.isParseSuccess()) {
                    rawTx = v.getRawTx().toString(2);
                    parsedTx = ArweaveTransaction.fromJSON(v.getRawTx());
                    mBinding.sign.setOnClickListener(x -> handleSign(v, saltLen));
                    initViewPager();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        viewModel.getObserverException().observe(this, (e) -> {
            if (e != null)
                alertException(e, this::navigateUp);
        });
    }

    private void initViewPager() {
        String[] title = {getString(R.string.overview), getString(R.string.raw)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = ArweaveParsedTxFragment.newInstance(parsedTx);
            fragments[1] = ArweaveTxDetailFragment.newInstance(rawTx);
        }
        mBinding.viewPager.setOffscreenPageLimit(2);
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void handleSign(ArweaveTxViewModel.Tx tx, int saltLen) {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    subscribeSignState(viewModel.handleSign(tx, saltLen));
                }, forgetPassword);
    }

    private void onSignSuccess(String txId) {
        mRepository.loadTx(txId).observe(this, v -> {
            Bundle data = new Bundle();
            UUID uuid = UUID.fromString(requestId);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            String signature = null;
            try {
                signature = new JSONObject(v.getAddition()).getString("signature");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArweaveSignature arweaveSignature = new ArweaveSignature(Hex.decode(signature), requestId);
            data.putString(KEY_SIGNATURE_UR, arweaveSignature.toUR().toString());
            navigate(R.id.action_to_arweaveBroadcastFragment, data);
        });
    }

    private void subscribeSignState(MutableLiveData<SignState> signState) {
        signState.observe(this, s -> {
            if (s == null) return;
            if (SignState.STATE_SIGNING.equals(s.getStatus())) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (SignState.STATE_SIGN_SUCCESS.equals(s.getStatus())) {
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
            } else if (SignState.STATE_SIGN_FAIL.equals(s.getStatus())) {
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

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
}
