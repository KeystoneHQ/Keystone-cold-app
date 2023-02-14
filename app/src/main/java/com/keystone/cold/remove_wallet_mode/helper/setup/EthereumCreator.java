package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Stream;

public class EthereumCreator extends BaseCreator {

    public EthereumCreator() {
        super(Coins.ETH);
    }

    @Override
    protected void update() {
        try {
            CoinEntity coinEntity = repository.loadCoinSync(coin.coinId());
            List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
            AccountEntity accountEntity = accountEntities.get(0);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("eth_account", ETHAccount.BIP44_STANDARD.getCode());
            accountEntity.setAddition(jsonObject.toString());
            accountEntity.setHdPath(ETHAccount.BIP44_STANDARD.getPath());
            repository.updateAccount(accountEntity);
            addAcccountForUpdate(ETHAccount.LEDGER_LIVE, coinEntity);
            addAcccountForUpdate(ETHAccount.LEDGER_LEGACY, coinEntity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addAcccountForUpdate(ETHAccount ethAccount, CoinEntity coin) throws JSONException {
        AccountEntity account = new AccountEntity();
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(ethAccount.getPath());
        account.setExPub(xPub);
        account.setHdPath(ethAccount.getPath());
        account.setCoinId(coin.getId());
        JSONObject json = new JSONObject();
        json.put("eth_account", ethAccount.getCode());
        account.setAddition(json.toString());
        long accountId = repository.insertAccount(account);
        account.setId(accountId);
        new EthereumAddressGenerator(ethAccount.getCode()).generateAddress(1);
    }

    @Override
    protected void generateDefaultAddress(CoinEntity entity) {
        Stream.of(ETHAccount.values())
                .forEach(ethAccount -> new EthereumAddressGenerator(ethAccount.getCode()).generateAddress(1));
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        try {
            boolean hasSetupStandard = false;
            for (int i = 0; i < coin.getAccounts().length; i++) {
                AccountEntity account = new AccountEntity();
                JSONObject jsonObject = new JSONObject();
                if (coin.getAccounts()[i].equals(ETHAccount.LEDGER_LIVE.getPath())) {
                    jsonObject.put("eth_account", ETHAccount.LEDGER_LIVE.getCode());
                } else if (coin.getAccounts()[i].equals(ETHAccount.BIP44_STANDARD.getPath())) {
                    if (!hasSetupStandard) {
                        hasSetupStandard = true;
                        jsonObject.put("eth_account", ETHAccount.BIP44_STANDARD.getCode());
                    } else {
                        jsonObject.put("eth_account", ETHAccount.LEDGER_LEGACY.getCode());
                    }
                }
                account.setAddition(jsonObject.toString());
                account.setHdPath(coin.getAccounts()[i]);
                entity.addAccount(account);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
