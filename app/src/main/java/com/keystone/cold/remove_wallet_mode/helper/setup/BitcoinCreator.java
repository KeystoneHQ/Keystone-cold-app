package com.keystone.cold.remove_wallet_mode.helper.setup;

import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCCoreNativeSegwitPath;
import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCLegacyPath;
import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCNativeSegwitPath;
import static com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel.BTCNestedSegwitPath;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinCoreNativeSegwitAddressGenerator;
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
        else if (coin.equals(Coins.BTC_LEGACY))
            new BitcoinLegacyAddressGenerator().generateAddress(1);
        else if (coin.equals(Coins.BTC_NATIVE_SEGWIT))
            new BitcoinNativeSegwitAddressGenerator().generateAddress(1);
        else if (coin.equals(Coins.BTC_CORE_WALLET))
            new BitcoinCoreNativeSegwitAddressGenerator().generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity account = new AccountEntity();
        if (coin.equals(Coins.BTC)) {
            String xpub = new GetExtendedPublicKeyCallable(BTCNestedSegwitPath).call();
            account.setHdPath(BTCNestedSegwitPath);
            account.setExPub(xpub);
        } else if (coin.equals(Coins.BTC_LEGACY)) {
            String xpub = new GetExtendedPublicKeyCallable(BTCLegacyPath).call();
            account.setHdPath(BTCLegacyPath);
            account.setExPub(xpub);
        } else if (coin.equals(Coins.BTC_NATIVE_SEGWIT)) {
            String xpub = new GetExtendedPublicKeyCallable(BTCNativeSegwitPath).call();
            account.setHdPath(BTCNativeSegwitPath);
            account.setExPub(xpub);
        } else if (coin.equals(Coins.BTC_CORE_WALLET)) {
            String xpub = new GetExtendedPublicKeyCallable(BTCCoreNativeSegwitPath).call();
            account.setHdPath(BTCNativeSegwitPath);
            account.setExPub(xpub);
        }
        entity.addAccount(account);
    }
}
