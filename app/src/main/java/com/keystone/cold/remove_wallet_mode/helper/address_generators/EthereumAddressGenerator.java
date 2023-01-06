package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import static com.keystone.cold.MainApplication.getApplication;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;

import org.json.JSONException;
import org.json.JSONObject;

public class EthereumAddressGenerator extends BaseAddressGenerator {

    public EthereumAddressGenerator() {
        coinId = Coins.ETH.coinId();
    }

    @Override
    protected String deriveAddress(int account, AddressEntity addressEntity, AbsDeriver deriver) {
        String code = Utilities.getCurrentEthAccount(getApplication());
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
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);
        try {
            JSONObject innerJson = new JSONObject();
            innerJson.put("xPub", xPub);
            JSONObject addition = new JSONObject();
            addition.put("addition", innerJson);
            addressEntity.setAddition(addition.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return address;
    }
}
