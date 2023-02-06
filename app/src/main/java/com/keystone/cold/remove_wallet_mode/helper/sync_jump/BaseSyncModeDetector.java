package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BaseSyncModeDetector implements SyncModeDetector {

    protected String coinId;

    @Override
    public void detect(Callback callback) {
        traverseAddress(coinId, callback);
    }


    private void traverseAddress(String coinId, Callback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DataRepository repository = MainApplication.getApplication().getRepository();
            List<AddressEntity> addressEntities = repository.loadAddressSync(coinId);
            addressEntities = addressEntities.stream()
                    .filter(this::filterSomeAddress)
                    .collect(Collectors.toList());
            int size = addressEntities.size();
            if (size == 0) {
                callback.invalid();
            } else if (size == 1) {
                callback.useDirect();
            } else {
                if (isSelectOneAddress()) {
                    callback.useSelectOneAddress();
                } else {
                    callback.useSelectAddress();
                }
            }
        });

    }

    //    If there is a need to filter addresses (for example, eth, sol,
    //    etc. need to filter addresses based on account), override this method
    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        return true;
    }

    // Override this method if only a single address can be selected for sync
    protected boolean isSelectOneAddress() {
        return false;
    }
}
