package com.keystone.cold.ui.fragment.setpassword;

import com.keystone.cold.R;
import com.keystone.cold.databinding.SetPasswordBinding;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;

public class BaseSetPasswordFragement extends SetupVaultBaseFragment<SetPasswordBinding> {

    public static final String PASSWORD = "password";
    public static final String SIGNATURE = "signature";
    public static final String MNEMONIC = "mnemonic";
    public static final String SLIP39_SEED = "slip39_seed";
    public static final String SLIP39_ID = "slip39_id";
    public static final String SHOULD_POP_BACK = "should_pop_back";

    @Override
    protected int setView() {
        return R.layout.set_password;
    }
}
