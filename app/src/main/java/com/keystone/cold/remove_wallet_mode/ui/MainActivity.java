package com.keystone.cold.remove_wallet_mode.ui;

import static com.keystone.cold.update.utils.Storage.hasSdcard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.ActivityMainRemoveWalletModeBinding;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.fingerprint.FingerprintKit;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.helper.PreGenerateAddressHelper;
import com.keystone.cold.remove_wallet_mode.helper.SetupManager;
import com.keystone.cold.remove_wallet_mode.ui.adapter.DrawerAdapter;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.MyAssetsFragment;
import com.keystone.cold.remove_wallet_mode.utils.SharePreferencesUtil;
import com.keystone.cold.remove_wallet_mode.utils.VersionUtils;
import com.keystone.cold.ui.common.FullScreenActivity;
import com.keystone.cold.ui.fragment.AboutFragment;
import com.keystone.cold.ui.fragment.main.AssetListFragment;
import com.keystone.cold.ui.fragment.setting.SettingFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.views.FullScreenDrawer;
import com.keystone.cold.ui.views.UpdatingHelper;
import com.keystone.cold.update.data.UpdateManifest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends FullScreenActivity {

    private ActivityMainRemoveWalletModeBinding mBinding;
    private NavController mNavController;

    private Toolbar toolbar;
    private final Handler mHandler = new Handler();

    private String belongTo;
    private String vaultId;

    int currentFragmentIndex = R.id.drawer_wallet;
    private DrawerAdapter drawerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_remove_wallet_mode);
        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt("currentFragmentIndex");
        }
        initViews();
        initNavController();
        belongTo = Utilities.getCurrentBelongTo(this);
        vaultId = Utilities.getVaultId(this);

        if (savedInstanceState == null) {
            if (hasSdcard()) {
                UpdatingHelper updatingHelper = new UpdatingHelper(this);
                MutableLiveData<UpdateManifest> manifestLiveData = updatingHelper.getUpdateManifest();
                manifestLiveData.observe(this, updateManifest -> {
                    if (updateManifest != null) {
                        updatingHelper.onUpdatingDetect(updateManifest);
                        manifestLiveData.removeObservers(this);
                    }
                });
            }
        }
        AppExecutors.getInstance().networkIO().execute(() -> {
            String mfp = new GetMasterFingerprintCallable().call();
            runOnUiThread(() -> mBinding.mfp.setText(String.format("Master Key Fingerprint：%s", mfp)));
            SetupManager.setup(null);
        });

        checkUpgrade();
        PreGenerateAddressHelper.preGenerateAddress();
    }

    private void checkUpgrade() {
        if (isNewVersion()) {
            setupNavController();
        }
    }

    // Version upgrade information is displayed only for version 10
    private boolean isNewVersion() {
        return UIConstants.VERSION_M_10.equals(VersionUtils.getVersion()) && TextUtils.isEmpty(SharePreferencesUtil.getFirmwareVersion(this));
    }

    private void setupNavController() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavInflater inflater = Objects.requireNonNull(navHostFragment).getNavController().getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.nav_graph_main_remove_wallet_mode);
        graph.setStartDestination(R.id.upgradeInformationFragment);
        navHostFragment.getNavController().setGraph(graph);
        SharePreferencesUtil.setFirmwareVersion(this, VersionUtils.getVersion());
    }

    private void initNavController() {
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String label = Objects.requireNonNull(destination.getLabel()).toString();
            int index = getFragmentIndexByLabel(label);
            Log.d("RemoveWalletMode", "index is " + index);
            if (index != -1) {
                mBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                currentFragmentIndex = index;
                if (drawerAdapter != null) {
                    drawerAdapter.setSelectIndex(currentFragmentIndex);
                }
            } else {
                mBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

        });
    }

    private int getFragmentIndexByLabel(String label) {
        Set<Map.Entry<Integer, String>> set = mMainFragments.entrySet();
        for (Map.Entry<Integer, String> entry : set) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragmentIndex", currentFragmentIndex);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return mNavController.navigateUp();
    }

    private void initViews() {
        mBinding.drawer.setScrimColor(Color.TRANSPARENT);

        drawerAdapter = new DrawerAdapter(currentFragmentIndex);
        drawerAdapter.setOnItemClickListener(new MainActivity.DrawerClickListener());
        mBinding.menu.setLayoutManager(new LinearLayoutManager(this));
        mBinding.menu.setAdapter(drawerAdapter);
        mBinding.menu.setItemViewCacheSize(0);
        mBinding.drawer.addDrawerListener(new FullScreenDrawer.DrawerListenerAdapter() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                mBinding.drawer.getChildAt(0).setX(mBinding.menuContainer.getWidth() + mBinding.menuContainer.getX());
            }
        });

        mBinding.llConnectSoftWareWallet.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConnectWalletActivity.class);
            startActivity(intent);
            mHandler.postDelayed(() -> {
                mBinding.drawer.closeDrawer(GravityCompat.START);
            }, 100);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String oldBelongTo = belongTo;
        belongTo = Utilities.getCurrentBelongTo(this);

        String oldVaultId = vaultId;
        vaultId = Utilities.getVaultId(this);

        if (!oldBelongTo.equals(belongTo) || !oldVaultId.equals(vaultId)) {
            recreate();
        }
    }

    public class DrawerClickListener implements DrawerAdapter.OnItemClickListener {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void itemClick(int position) {
            if (currentFragmentIndex == position) {
                mBinding.drawer.closeDrawer(GravityCompat.START);
                return;
            }
            switch (position) {
                case R.id.drawer_wallet:
                    boolean isPopped = mNavController.popBackStack(R.id.myAssetsFragment, false);
                    if (!isPopped) {
                        mNavController.navigate(R.id.myAssetsFragment);
                    }
                    break;
                case R.id.drawer_settings:
                    //mNavController.navigateUp();
                    mNavController.navigate(R.id.settingFragment);
                    break;
                case R.id.drawer_about:
                    //mNavController.navigateUp();
                    mNavController.navigate(R.id.aboutFragment);
                    break;
            }
            mHandler.postDelayed(() -> mBinding.drawer.closeDrawer(GravityCompat.START), 100);
        }
    }

    public NavController getNavController() {
        return mNavController;
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawer.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawer.closeDrawer(GravityCompat.START);
        } else {
            NavDestination destination = mNavController.getCurrentDestination();
            if (destination != null && destination.getLabel() != null) {
                if (AssetListFragment.TAG.equals(destination.getLabel().toString())) {
                    return;
                }
            }
            super.onBackPressed();
        }
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        boolean supportFingerprint = FingerprintKit.isHardwareDetected(this);
        if (!Utilities.hasUserClickPatternLock(this)
                || (supportFingerprint && !Utilities.hasUserClickFingerprint(this))) {
            this.toolbar = toolbar;
            //showBadge(toolbar);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateBadge() {
        boolean supportFingerprint = FingerprintKit.isHardwareDetected(this);
        if (Utilities.hasUserClickPatternLock(this)
                && (!supportFingerprint || Utilities.hasUserClickFingerprint(this))) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
        }
        drawerAdapter.notifyDataSetChanged();
    }

    private void showBadge(@Nullable Toolbar toolbar) {
        Drawable menu = Objects.requireNonNull(toolbar).getNavigationIcon();
        int badgeSize = (int) getResources().getDimension(R.dimen.default_badge_size);
        int radius = badgeSize / 2;

        int width = Objects.requireNonNull(menu).getIntrinsicWidth() + badgeSize;
        int height = menu.getIntrinsicHeight() + badgeSize;

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        menu.setBounds(radius, radius, width - radius, height - radius);
        menu.draw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle(width - radius, radius, radius, paint);
        toolbar.setNavigationIcon(new BitmapDrawable(getResources(), bitmap));
    }

    public void toggleDrawer(View v) {
        if (mBinding.drawer.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawer.closeDrawer(GravityCompat.START);
        } else {
            mBinding.drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NavDestination nav = getNavController().getCurrentDestination();
        if (nav != null) {
            if (securePages.contains(nav.getId())) {
                getNavController().popBackStack(R.id.settingFragment, false);
            }
        }
    }

    private final List<Integer> securePages = Arrays.asList(
            R.id.fingerprintSettingFragment,
            R.id.fingerprintManageFragment,
            R.id.fingerprintEnrollFragment,
            R.id.fingerprintGuideFragment,
            R.id.setPasswordFragment,
            R.id.setPatternUnlockFragment);

    private final static Map<Integer, String> mMainFragments = new HashMap<>();

    static {
        mMainFragments.put(R.id.drawer_wallet, MyAssetsFragment.TAG);
        mMainFragments.put(R.id.drawer_settings, SettingFragment.TAG);
        mMainFragments.put(R.id.drawer_about, AboutFragment.TAG);
    }

    public void notice(
            String title,
            String subTitle,
            String buttonText,
            Runnable confirmAction) {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.common_modal, null, false);
        binding.title.setText(title);
        binding.subTitle.setText(subTitle);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(buttonText);
        binding.confirm.setOnClickListener(v -> {
            if (confirmAction != null) {
                confirmAction.run();
            }
        });
        dialog.setBinding(binding);
        dialog.show(getSupportFragmentManager(), "");
    }

    public void restart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

