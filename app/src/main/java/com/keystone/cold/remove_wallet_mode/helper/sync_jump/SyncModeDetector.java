package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

public interface SyncModeDetector {
    interface Callback {
        void useDirect();

        void useSelectAddress();

        void useSelectOneAddress();

        void invalid();
    }

    void detect(Callback callback);

}
