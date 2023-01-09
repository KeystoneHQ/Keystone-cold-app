package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;


// Should only use this exception in dev time.
public class UnimplementedException extends BaseException {
    public UnimplementedException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static UnimplementedException newInstance() {
        return new UnimplementedException("Function not implemented yet", "function not implemented");
    }

    @Override
    public String getTitle() {
        return "Unimplemented";
    }
}
