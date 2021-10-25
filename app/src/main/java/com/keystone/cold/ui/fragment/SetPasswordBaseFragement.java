package com.keystone.cold.ui.fragment;

import static com.keystone.cold.viewmodel.SetupVaultViewModel.PasswordValidationResult.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ChangePasswordCallable;
import com.keystone.cold.callables.ResetPasswordCallable;
import com.keystone.cold.databinding.ModalBinding;
import com.keystone.cold.databinding.SetPasswordBinding;
import com.keystone.cold.selfcheck.RuntimeStatusCode;
import com.keystone.cold.ui.AttackWarningActivity;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.util.HashUtil;
import com.keystone.cold.util.Keyboard;
import com.keystone.cold.viewmodel.SetupVaultViewModel;

import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

public abstract class SetPasswordBaseFragement extends SetupVaultBaseFragment<SetPasswordBinding> {
    public static final String PASSWORD = "password";
    public static final String SIGNATURE = "signature";
    public static final String MNEMONIC = "mnemonic";
    public static final String SLIP39_SEED = "slip39_seed";
    public static final String SLIP39_ID = "slip39_id";
    public static final String SHOULD_POP_BACK = "should_pop_back";

    private boolean deleteAll;
    private boolean inputValid;
    private boolean paused;
    private String passwordHash;
    private View.OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
        if (!hasFocus) {
            if (!paused) {
                SetupVaultViewModel.PasswordValidationResult result = viewModel.validatePassword();
                if (result != RESULT_OK) {
                    mBinding.hint.setTextColor(mActivity.getColor(R.color.red));
                    mBinding.hint.setText(getHint(result));
                    deleteAll = true;
                    inputValid = false;
                } else {
                    inputValid = true;
                }
            }
        } else {
            mBinding.hint.setTextColor(mActivity.getColor(R.color.white));
            mBinding.hint.setText(R.string.text_password_required);
        }
    };
    private View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (deleteAll) {
                mBinding.pwd1.setText("");
                deleteAll = false;
            }
        }
        return false;
    };
    private Observable.OnPropertyChangedCallback changedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            String password1 = viewModel.getPwd1().get();
            String password2 = viewModel.getPwd2().get();
            if (password2.length() >= password1.length()) {
                if (password1.equals(password2)) {
                    if (inputValid) {
                        mBinding.confirm.setEnabled(true);
                    } else {
                        mBinding.confirm.setEnabled(false);
                    }
                    mBinding.hint2.setVisibility(View.GONE);
                } else {
                    mBinding.hint2.setVisibility(View.VISIBLE);
                    mBinding.confirm.setEnabled(false);
                }
            } else {
                mBinding.hint2.setVisibility(View.GONE);
                mBinding.confirm.setEnabled(false);
            }
        }
    };

    @Override
    protected int setView() {
        return R.layout.set_password;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.confirm.setEnabled(false);
        paused = false;
        Keyboard.show(mActivity, mBinding.pwd1);
        mBinding.pwd1.setOnFocusChangeListener(onFocusChangeListener);
        mBinding.pwd1.setOnKeyListener(onKeyListener);
        viewModel.getPwd1().addOnPropertyChangedCallback(changedCallback);
        viewModel.getPwd2().addOnPropertyChangedCallback(changedCallback);
        mBinding.confirm.setOnClickListener(v -> validatePassword());
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.getPwd1().set("");
        viewModel.getPwd2().set("");
        paused = true;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Keyboard.show(mActivity, mBinding.pwd1);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mBinding.setViewModel(viewModel);
        mBinding.pwd1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        mBinding.pwd2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
    }

    private int getHint(SetupVaultViewModel.PasswordValidationResult result) {
        int hintRes = 0;
        switch (result) {
            case RESULT_TOO_SHORT:
                hintRes = R.string.password_verify_too_short;
                break;
            case RESULT_NOT_MATCH:
                hintRes = R.string.password_verify_wrong;
                break;
            case RESULT_INPUT_WRONG:
                hintRes = R.string.password_input_wrong;
                break;
        }
        return hintRes;
    }

    private void validatePassword() {
        SetupVaultViewModel.PasswordValidationResult result = viewModel.validatePassword();
        if (result != RESULT_OK) {
            showInvalidPasswordHint(result);
        } else {
            setPassword();
        }
    }

    private void setPassword() {
        Handler handler = new Handler();
        if (!TextUtils.isEmpty(viewModel.getPwd1().get())) {
            mBinding.confirm.setVisibility(View.GONE);
            mBinding.progress.setVisibility(View.VISIBLE);
            AppExecutors.getInstance().networkIO().execute(() -> {
                String password = Objects.requireNonNull(viewModel.getPwd1().get());
                passwordHash = Hex.toHexString(Objects.requireNonNull(HashUtil.twiceSha256(password)));
                handler.post(() -> {
                    Keyboard.hide(mActivity, mBinding.pwd2);
                    setPasswordAndJump();
                });
            });
        }
    }

    public void changePasswordCallable(String currentPassword) {
        if (!new ChangePasswordCallable(passwordHash, currentPassword).call()) {
            handleRuntimeStateAbnormal(mActivity, RuntimeStatusCode.RUNTIME_WRITE_PASSWORD_FAILED);
        }
    }

    public void resetPassword(@Nullable String mnemonic, @Nullable String slip39MasterSeed, int slip39Id) {
        if (!new ResetPasswordCallable(passwordHash, mnemonic, slip39MasterSeed, slip39Id).call()) {
            handleRuntimeStateAbnormal(mActivity, RuntimeStatusCode.RUNTIME_WRITE_PASSWORD_FAILED);
        }
    }

    public abstract void setPasswordAndJump();

    private void showInvalidPasswordHint(SetupVaultViewModel.PasswordValidationResult result) {
        ModalBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(mActivity), R.layout.modal, null, false);
        ModalDialog modal = ModalDialog.newInstance();
        modal.setBinding(binding);

        int hintRes = R.string.password_verify_too_short;
        int iconRes = R.drawable.circle_info;
        switch (result) {
            case RESULT_TOO_SHORT:
                hintRes = R.string.password_verify_too_short;
                iconRes = R.drawable.circle_info;
                break;
            case RESULT_NOT_MATCH:
            case RESULT_INPUT_WRONG:
                hintRes = R.string.password_verify_wrong;
                iconRes = R.drawable.circle_info;
                break;
        }
        binding.text.setText(hintRes);
        binding.icon.setImageResource(iconRes);
        modal.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        new Handler().postDelayed(modal::dismiss, 3000);
    }

    public static void handleRuntimeStateAbnormal(Activity activity, int statusCode) {
        Utilities.setAttackDetected(activity);
        Bundle data = new Bundle();
        data.putInt("firmware", statusCode);
        data.putInt("system", 0);
        data.putInt("signature", 0);
        Intent intent = new Intent(activity, AttackWarningActivity.class);
        intent.putExtras(data);
        activity.startActivity(intent);
    }
}
