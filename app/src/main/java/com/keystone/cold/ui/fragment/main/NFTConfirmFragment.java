package com.keystone.cold.ui.fragment.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.NftConfirmFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

import java.util.Arrays;

public class NFTConfirmFragment extends BaseFragment<NftConfirmFragmentBinding> {

    public static String KEY_CHAIN_ID = "CHAIN_ID";
    public static String KEY_CONTRACT_ADDRESS = "CONTRACT_ADDRESS";
    public static String KEY_NAME = "NAME";
    public static String KEY_CONTRACT_NAME = "CONTRACT_NAME";
    public static String KEY_MEDIA_DATA = "MEDIA_DATA";

    private Integer chainId;
    private String contractAddress;
    private String name;
    private String contractName;
    private String mediaData;

    private Web3TxViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.nft_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        chainId = requireArguments().getInt(KEY_CHAIN_ID);
        contractAddress = requireArguments().getString(KEY_CONTRACT_ADDRESS);
        contractName = requireArguments().getString(KEY_CONTRACT_NAME);
        name = requireArguments().getString(KEY_NAME);
        mediaData = requireArguments().getString(KEY_MEDIA_DATA);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        try {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
            mBinding.network.setText(viewModel.getNetwork(chainId));
            mBinding.name.setText(name);
            mBinding.contractName.setText(contractName);
            mBinding.contractAddress.setText(contractAddress);

            mBinding.confirm.setOnClickListener(v -> {
                Utilities.setNftAvatarResource(mActivity, mediaData);
                navigate(R.id.action_to_home);
            });

            byte[] decodedImage = Base64.decode(mediaData, Base64.DEFAULT);
            Bitmap nftImage = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            mBinding.nftImage.setImageBitmap(nftImage);
        } catch (IllegalArgumentException e) {
            ModalDialog.showCommonModal(mActivity, getString(R.string.invalid_data), getString(R.string.import_nft_error), getString(R.string.confirm), ()-> {
                navigate(R.id.action_to_home);
            });
        }
    }
}
