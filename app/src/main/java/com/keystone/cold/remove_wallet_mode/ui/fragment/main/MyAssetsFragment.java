package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentMyAssetsBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.remove_wallet_mode.ui.adapter.CoinAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AssetViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.PolkadotViewModel;

import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyAssetsFragment extends BaseFragment<FragmentMyAssetsBinding> {
    public static final String TAG = "MyAssetsFragment";

    private CoinAdapter coinAdapter;

    private final CoinAdapter.CoinClickCallback mCoinClickCallback = assetItem -> {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_COIN_ID, assetItem.getCoinId());
        bundle.putString(KEY_COIN_CODE, assetItem.getCoinCode());
        navigate(R.id.action_to_assetFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.fragment_my_assets;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        coinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, false);
        mBinding.assetList.setAdapter(coinAdapter);
        mBinding.ivScan.setOnClickListener((v) -> {
            navigate(R.id.action_to_scanner);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        AssetViewModel assetViewModel = ViewModelProviders.of(mActivity).get(AssetViewModel.class);
        subscribeUi(assetViewModel.loadAssets());
        hookSubstrateAddresses();
    }

    private void subscribeUi(LiveData<List<AssetItem>> assets) {
        assets.observe(this, assetItems -> {
            if (assetItems != null) {
                coinAdapter.setItems(filterDisplayCoins(assetItems));
            }
        });
    }

    private List<AssetItem> filterDisplayCoins(List<AssetItem> assetItems) {
        return assetItems.stream().filter(AssetItem::isShow).collect(Collectors.toList());
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.more, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        binding.rlAddHideAsset.setVisibility(View.VISIBLE);
        binding.rlAddHideAsset.setOnClickListener(v -> {
            navigate(R.id.action_to_manageCoinFragment);
            dialog.dismiss();

        });
        binding.rlFAQ.setVisibility(View.VISIBLE);
        binding.rlFAQ.setOnClickListener(v -> {
            navigate(R.id.action_to_faqFragment);
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void hookSubstrateAddresses() {
        AppExecutors.getInstance().diskIO().execute(() -> {});
        PolkadotViewModel polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
        DataRepository mRepository = MainApplication.getApplication().getRepository();
        try {
            polkadotViewModel.initialDB();
            LiveData<List<AddressEntity>> dot = mRepository.loadAddress(Coins.DOT.coinId());
            dot.observe(this, addressEntities -> addressEntities.forEach((a) -> {
                try {
                    byte[] pubkey = AddressCodec.decodeAddress(a.getAddressString());
                    String publicKey = Hex.toHexString(pubkey);
                    String path = a.getPath();
                    polkadotViewModel.importAddress(publicKey, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dot.removeObservers(this);
            }));
            LiveData<List<AddressEntity>> ksm = mRepository.loadAddress(Coins.KSM.coinId());
            ksm.observe(this, addressEntities -> addressEntities.forEach((a) -> {
                try {
                    byte[] pubkey = AddressCodec.decodeAddress(a.getAddressString());
                    String publicKey = Hex.toHexString(pubkey);
                    String path = a.getPath();
                    polkadotViewModel.importAddress(publicKey, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ksm.removeObservers(this);
            }));
        } catch (PolkadotViewModel.PolkadotException e) {
            e.printStackTrace();
        }
    }
}
