package com.keystone.cold.ui.fragment.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.NftConfirmFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

public class NFTConfirmFragment extends BaseFragment<NftConfirmFragmentBinding> {


    public final static String ETH_NFT = "ETH_NFT";
    public final static String SOL_NFT = "SOL_NFT";

    public static String KEY_CHAIN_ID = "CHAIN_ID";
    public static String KEY_CONTRACT_ADDRESS = "CONTRACT_ADDRESS";
    public static String KEY_NAME = "NAME";
    public static String KEY_CONTRACT_NAME = "CONTRACT_NAME";
    public static String KEY_MEDIA_DATA = "MEDIA_DATA";

    public static String KEY_MINT_ADDRESS = "MINT_ADDRESS";
    public static String KEY_COLLECTION_NAME = "COLLECTION_NAME";

    public static String KEY_NFT_TYPE = "NFT_TYPE";

    private Integer chainId;
    private String contractAddress;
    private String name;
    private String contractName;
    private String mediaData;

    private String mintAddress;
    private String collectionName;
    private String nftType;

    private Web3TxViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.nft_confirm_fragment;
    }

    @Override
    protected void init(View view) {

        nftType = requireArguments().getString(KEY_NFT_TYPE);

        if (ETH_NFT.equals(nftType)) {
            viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
            chainId = requireArguments().getInt(KEY_CHAIN_ID);
            contractAddress = requireArguments().getString(KEY_CONTRACT_ADDRESS);
            contractName = requireArguments().getString(KEY_CONTRACT_NAME);
            name = requireArguments().getString(KEY_NAME);
            mediaData = requireArguments().getString(KEY_MEDIA_DATA);
        } else if (SOL_NFT.equals(nftType)) {
            mintAddress = requireArguments().getString(KEY_MINT_ADDRESS);
            collectionName = requireArguments().getString(KEY_COLLECTION_NAME);
            name = requireArguments().getString(KEY_NAME);
            mediaData = requireArguments().getString(KEY_MEDIA_DATA);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        try {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
            if (ETH_NFT.equals(nftType)) {
                mBinding.network.setText(viewModel.getNetwork(chainId));
                mBinding.name.setText(name);
                mBinding.contractName.setText(contractName);
                mBinding.tvAddressName.setText(R.string.import_nft_contract_address);
                mBinding.contractAddress.setText(contractAddress);
            } else if (SOL_NFT.equals(nftType)) {
                mBinding.network.setVisibility(View.GONE);
                mBinding.name.setText(name);
                mBinding.contractName.setText(collectionName);
                mBinding.tvAddressName.setText(R.string.import_nft_mint_address);
                mBinding.contractAddress.setText(mintAddress);
            }

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
