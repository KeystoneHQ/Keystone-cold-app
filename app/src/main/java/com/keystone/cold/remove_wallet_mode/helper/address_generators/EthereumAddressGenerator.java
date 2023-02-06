package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EthereumAddressGenerator extends MultiAccountAddressGenerator {

    public EthereumAddressGenerator(String code) {
        super(code);
        coinId = Coins.ETH.coinId();
    }

    @Override
    protected String deriveAddress(int account, AddressEntity addressEntity, AbsDeriver deriver) {
        ETHAccount ethAccount = ETHAccount.ofCode(code);
        String path;
        switch (ethAccount) {
            case LEDGER_LIVE:
                path = ethAccount.getPath() + "/" + account + "'/0/0";
                break;
            case LEDGER_LEGACY:
                path = ethAccount.getPath() + "/" + account;
                break;
            default:
                path = ethAccount.getPath() + "/0/" + account;
        }
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(path);
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);
        return address;
    }

    @Override
    protected AccountEntity getAccountByRule(List<AccountEntity> accountEntities) {
        ETHAccount account = ETHAccount.ofCode(code);
        try {
            for (int i = 0; i < accountEntities.size(); i++) {
                JSONObject jsonObject = new JSONObject(accountEntities.get(i).getAddition());
                if (jsonObject.get("eth_account").equals(account.getCode())) {
                    return accountEntities.get(i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAddress(int index, String code) {
        ETHAccount ethAccount = ETHAccount.ofCode(code);
        String path;
        switch (ethAccount) {
            case LEDGER_LIVE:
                path = ethAccount.getPath() + "/" + index + "'/0/0";
                break;
            case LEDGER_LEGACY:
                path = ethAccount.getPath() + "/" + index;
                break;
            default:
                path = ethAccount.getPath() + "/0/" + index;
        }
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        AbsDeriver deriver = AbsDeriver.newInstance(Coins.ETH.coinCode());
        assert deriver != null;
        return deriver.derive(xPub);
    }
}
