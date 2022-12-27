package com.keystone.cold.remove_wallet_mode.helper.sync_step;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BaseAddressDetector implements AddressDetector {

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
                AppExecutors.getInstance().mainThread().execute(callback::noAddress);
            } else if (size == 1) {
                AppExecutors.getInstance().mainThread().execute(callback::oneAddress);
            } else {
                AppExecutors.getInstance().mainThread().execute(callback::moreThanOneAddress);
            }
        });

    }

    //    If there is a need to filter addresses (for example, eth, sol,
    //    etc. need to filter addresses based on account), override this method
    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        return true;
    }

}
