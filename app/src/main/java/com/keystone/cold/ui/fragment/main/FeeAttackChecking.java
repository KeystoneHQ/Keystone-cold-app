package com.keystone.cold.ui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.Objects;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;

public class FeeAttackChecking {

    public static final String KEY_DUPLICATE_TX = "key_duplicate_tx";
    public interface FeeAttackCheckingResult {

        int NORMAL = 1;
        int DUPLICATE_TX = 2;
        int SAME_OUTPUTS = 3;
    }
    private BaseFragment fragment;

    public FeeAttackChecking(BaseFragment fragment) {
        this.fragment = fragment;
    }

    public void showFeeAttackWarning() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(fragment.getHostActivity()), R.layout.common_modal,
                null, false);
        modalDialog.setBinding(binding);
        binding.title.setText(R.string.abnormal_tx);
        binding.subTitle.setText(R.string.fee_attack_warning);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(v -> modalDialog.dismiss());
        modalDialog.show(fragment.getHostActivity().getSupportFragmentManager(),"");
    }

    private void navigateToSignedTx(String txId, String signId) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_ID, txId);
        bundle.putBoolean(KEY_DUPLICATE_TX,true);
        if (ELECTRUM_SIGN_ID.equals(signId)) {
            Navigation.findNavController(Objects.requireNonNull(fragment.getView()))
                    .navigate(R.id.action_to_electrumTxFragment, bundle);
        } else {
            Navigation.findNavController(Objects.requireNonNull(fragment.getView()))
                    .navigate(R.id.action_to_txFragment, bundle);
        }
    }

    public void showDuplicateTx(TxEntity tx) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(fragment.getHostActivity()), R.layout.common_modal,
                null, false);
        modalDialog.setBinding(binding);
        binding.title.setText(R.string.broadcast_tx);
        binding.close.setVisibility(View.GONE);
        binding.subTitle.setText(R.string.already_signed);
        binding.confirm.setText(R.string.broadcast_tx);
        binding.confirm.setOnClickListener(v -> {
            modalDialog.dismiss();
            navigateToSignedTx(tx.getTxId(), tx.getSignId());
        });
        modalDialog.show(fragment.getHostActivity().getSupportFragmentManager(),"");
    }
}
