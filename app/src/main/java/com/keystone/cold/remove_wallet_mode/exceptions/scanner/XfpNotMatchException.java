package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class XfpNotMatchException extends BaseException {

    public static XfpNotMatchException newInstance() {
        return new XfpNotMatchException("Master fingerprint not match", "master fingerprint not match");
    }

    public XfpNotMatchException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.xfp_not_match);
    }
}
