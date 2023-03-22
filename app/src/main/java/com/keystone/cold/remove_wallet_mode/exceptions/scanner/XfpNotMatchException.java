package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class XfpNotMatchException extends BaseException {

    public static XfpNotMatchException newInstance() {
        return new XfpNotMatchException(CONTEXT.getString(R.string.mfp_not_match), "master fingerprint not match");
    }

    public XfpNotMatchException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.xfp_not_match);
    }
}
