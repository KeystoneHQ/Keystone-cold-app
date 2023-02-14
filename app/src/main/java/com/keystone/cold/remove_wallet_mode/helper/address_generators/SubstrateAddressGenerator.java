package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class SubstrateAddressGenerator extends BaseAddressGenerator {
    private String rootPath; // path: //polkadot | //kusama | etc..

    public SubstrateAddressGenerator(Coins.Coin coin) {
        this.coinId = coin.coinId();
        this.rootPath = coin.getAccounts()[0];
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String path = rootPath;
        if (index != 0) {
            path = rootPath + "/" + index;
        }
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(path);
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
        return deriver.derive(xPub);
    }
}
