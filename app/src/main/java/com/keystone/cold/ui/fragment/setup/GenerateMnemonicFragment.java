/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.ui.fragment.setup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.navigation.fragment.NavHostFragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.GenerateMnemonicBinding;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SecretModalDialog;

import static com.keystone.cold.mnemonic.MnemonicInputTable.TWEENTYFOUR;

public class GenerateMnemonicFragment extends SetupVaultBaseFragment<GenerateMnemonicBinding> {

    private SecretModalDialog dialog;
    private boolean useDice;
    private byte[] diceRolls;
    private boolean seedPick;
    private String incompleteMnemonic;
    private boolean isSharding;
    private int shardingSequence;

    @Override
    protected int setView() {
        return R.layout.generate_mnemonic;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPress());
        mBinding.table.setMnemonicNumber(viewModel.getMnemonicCount().get());
        mBinding.table.setEditable(false);
        if (viewModel.isShardingMnemonic()) {
            initSharding();
        } else {
            generateRandomMnemonic();
        }
        mBinding.confirmSaved.setOnClickListener(v -> confirmInput());
    }

    private void generateRandomMnemonic() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            useDice = bundle.getBoolean("use_dice");
            diceRolls = bundle.getByteArray("dice_rolls");
            seedPick = bundle.getBoolean("seed_pick");
            incompleteMnemonic = bundle.getString("words");
        }
        if (useDice) {
            viewModel.generateMnemonicFromDiceRolls(diceRolls);
        } else if(seedPick) {
            mBinding.table.setMnemonicNumber(TWEENTYFOUR);
            viewModel.completeMnemonic(incompleteMnemonic);
        } else {
            viewModel.generateRandomMnemonic();
        }
        observeMnemonic();
    }

    private void initSharding() {
        int shardingSequence = viewModel.currentSequence();
        mBinding.shardingHint.setVisibility(View.VISIBLE);
        mBinding.shardingHint.setText(getString(R.string.generate_sharding_sequence,
                shardingSequence + 1, viewModel.totalShares()));
        String[] words = viewModel.getShareByIndex(shardingSequence).split(" ");
        for (int i = 0; i < words.length; i++) {
            mBinding.table.getWordsList().get(i).set(words[i]);
        }
        mBinding.confirmSaved.setEnabled(true);
    }

    private void onBackPress() {
        if (useDice) {
            NavHostFragment.findNavController(this)
                    .popBackStack(R.id.rollingDiceGuideFragment,false);
        } else {
            super.navigateUp();
        }
    }

    private void confirmInput() {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        dialog.setBinding(binding);
        binding.title.setText(R.string.please_confirm);
        binding.subTitle.setText(R.string.mnemonic_save_hint);
        binding.confirm.setText(R.string.confirm_to_verify_mnemonic);
        binding.confirm.setOnClickListener(v -> {
            dialog.dismiss();
            navigate(R.id.action_to_confirmMnemonicFragment);
        });
        binding.close.setOnClickListener(v -> dialog.dismiss());
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void observeMnemonic() {
        viewModel.getMnemonic().observe(this, s -> {
            if (TextUtils.isEmpty(s)) {
                return;
            }
            String[] words = s.split(" ");
            if (words.length != 24) {
                return;
            }
            if (seedPick) {
                mBinding.lastWordHint.setVisibility(View.VISIBLE);
                mBinding.lastWordHint.setText(getString(R.string.last_word_hint, words[23]));
            }

            if (dialog == null) {
                dialog = new SecretModalDialog();
            }

            if (dialog.getDialog() == null || !dialog.getDialog().isShowing()) {
                dialog.show(mActivity.getSupportFragmentManager(), "");
            }

            for (int i = 0; i < words.length; i++) {
                mBinding.table.getWordsList().get(i).set(words[i]);
            }
            mBinding.confirmSaved.setEnabled(true);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.getMnemonic().removeObservers(this);
    }
}
