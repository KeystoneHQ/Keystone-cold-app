package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

public interface AddressDetector {
    interface Callback {
        void oneAddress();

        void moreThanOneAddress();

        void noAddress();
    }

    void detect(Callback callback);

}
