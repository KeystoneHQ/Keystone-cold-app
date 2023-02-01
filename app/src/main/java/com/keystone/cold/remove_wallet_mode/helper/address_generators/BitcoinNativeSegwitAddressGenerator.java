package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;

public class BitcoinNativeSegwitAddressGenerator extends BaseAddressGenerator {
    public BitcoinNativeSegwitAddressGenerator() {
        this.coinId = Coins.BTC_NATIVE_SEGWIT.coinId();
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String path = BitcoinTxViewModel.BTCNativeSegwitPath + "/0/" + index;
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);
        return address;
    }

    public static String getAddress(int index) {
        String path = BitcoinTxViewModel.BTCNativeSegwitPath + "/0/" + index;
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        AbsDeriver deriver = AbsDeriver.newInstance(Coins.BTC_NATIVE_SEGWIT.coinCode());
        assert deriver != null;
        return deriver.derive(xPub);
    }
}
