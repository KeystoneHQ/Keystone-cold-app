package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.Stream;

public class SolanaCreator extends ED25519CoinCreator{

    public SolanaCreator() {
        super(Coins.SOL);
    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        Stream.of(SOLAccount.values())
                .forEach(solAccount -> new SolanaAddressGenerator(solAccount.getCode()).generateAddress(1));
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        try {
            for (int i = 0; i < coin.getAccounts().length; i++) {
                AccountEntity account = new AccountEntity();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sol_account", coin.getAccounts()[i]);
                account.setAddition(jsonObject.toString());
                account.setHdPath(SOLAccount.getPathByCode(coin.getAccounts()[i]));
                entity.addAccount(account);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
