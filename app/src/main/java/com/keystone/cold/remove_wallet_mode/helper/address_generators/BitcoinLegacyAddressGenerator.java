package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;

public class BitcoinLegacyAddressGenerator extends BaseAddressGenerator{
    public BitcoinLegacyAddressGenerator() {
        this.coinId = Coins.BTC_LEGACY.coinId();
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String path = BitcoinTxViewModel.BTCLegacyPath + "/0/" + index;
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);
        return address;
    }

    public static String getAddress(int index) {
        String path = BitcoinTxViewModel.BTCLegacyPath + "/0/" + index;
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        AbsDeriver deriver = AbsDeriver.newInstance(Coins.BTC_LEGACY.coinCode());
        assert deriver != null;
        return deriver.derive(xPub);
    }
}
