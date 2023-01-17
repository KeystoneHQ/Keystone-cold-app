package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.SolanaAddressFilter;


public class SolFlareViewModel extends BaseCryptoMultiAccountsSyncViewModel {

    public SolFlareViewModel(@NonNull Application application) {
        super(application);
        coinId = Coins.SOL.coinId();
    }


    @Override
    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        if (new SolanaAddressFilter().filter(addressEntity)) {
            return super.filterSomeAddress(addressEntity);
        }
        return false;
    }
}
