package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import android.os.Bundle;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;

public class UOSProcessor implements Processor {
    @Override
    public Destination run(ScanResult r) throws BaseException {
        String res = r.getData();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SIGN_DATA_KEY, res);
        return new Destination(R.id.action_to_substrateConfirmTransactionFragment, bundle);
    }
}
