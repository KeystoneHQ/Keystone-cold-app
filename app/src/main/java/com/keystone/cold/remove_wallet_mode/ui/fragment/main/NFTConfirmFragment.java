package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentNftConfirmBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

public class NFTConfirmFragment extends BaseFragment<FragmentNftConfirmBinding> {


    public final static String ETH_NFT = "ETH_NFT";
    public final static String SOL_NFT = "SOL_NFT";


    private Long chainId;
    private String contractAddress;
    private String name;
    private String contractName;
    private String mediaData;

    private String mintAddress;
    private String collectionName;
    private String nftType;


    @Override
    protected int setView() {
        return R.layout.fragment_nft_confirm;
    }

    @Override
    protected void init(View view) {
        nftType = requireArguments().getString(BundleKeys.NFT_TYPE_KEY);
        if (ETH_NFT.equals(nftType)) {
            chainId = requireArguments().getLong(BundleKeys.CHAIN_ID_KEY);
            contractAddress = requireArguments().getString(BundleKeys.CONTRACT_ADDRESS_KEY);
            contractName = requireArguments().getString(BundleKeys.CONTRACT_NAME_KEY);
            name = requireArguments().getString(BundleKeys.NAME_KEY);
            mediaData = requireArguments().getString(BundleKeys.MEDIA_DATA_KEY);
        } else if (SOL_NFT.equals(nftType)) {
            mintAddress = requireArguments().getString(BundleKeys.MINT_ADDRESS_KEY);
            collectionName = requireArguments().getString(BundleKeys.COLLECTION_NAME_KEY);
            name = requireArguments().getString(BundleKeys.NAME_KEY);
            mediaData = requireArguments().getString(BundleKeys.MEDIA_DATA_KEY);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        try {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
            if (ETH_NFT.equals(nftType)) {
                mBinding.network.setText(EthereumTxViewModel.getNetwork(chainId));
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
