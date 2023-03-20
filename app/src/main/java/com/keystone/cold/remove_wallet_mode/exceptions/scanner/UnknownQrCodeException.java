package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class UnknownQrCodeException extends BaseException {
    public UnknownQrCodeException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static UnknownQrCodeException newInstance() {
        return new UnknownQrCodeException(CONTEXT.getString(R.string.invalid_qr_code_hint), "Invalid QR code");
    }

    @Override
    public String getTitle() {
        return MainApplication.getApplication().getString(R.string.invalid_qrcode);
    }
}
