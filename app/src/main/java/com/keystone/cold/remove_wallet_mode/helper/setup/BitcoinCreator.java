package com.keystone.cold.remove_wallet_mode.helper.setup;

import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCLegacyPath;
import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCNativeSegwitPath;
import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCNestedSegwitPath;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinLegacyAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNestedSegwitAddressGenerator;

public class BitcoinCreator extends BaseCreator {
    public BitcoinCreator(Coins.Coin coin) {
        super(coin);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        if (coin.equals(Coins.BTC)) new BitcoinNestedSegwitAddressGenerator().generateAddress(1);
        else if (coin.equals(Coins.BTC_LEGACY)) new BitcoinLegacyAddressGenerator().generateAddress(1);
        else if (coin.equals(Coins.BTC_NATIVE_SEGWIT)) new BitcoinNativeSegwitAddressGenerator().generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity account = new AccountEntity();
        if (coin.equals(Coins.BTC)) account.setHdPath(BTCNestedSegwitPath);
        else if (coin.equals(Coins.BTC_LEGACY)) account.setHdPath(BTCLegacyPath);
        else if (coin.equals(Coins.BTC_NATIVE_SEGWIT)) account.setHdPath(BTCNativeSegwitPath);
        entity.addAccount(account);
    }
}
