package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class SuiAddressGenerator extends BaseAddressGenerator {

    public SuiAddressGenerator() {
        coinId = Coins.SUI.coinId();
    }

    @Override
    protected String deriveAddress(int account, AddressEntity addressEntity, AbsDeriver deriver) {
        String xPubPath = "M/44'/784'/" + account + "'/0'/0'";
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(xPubPath);
        String address = deriver.derive(xPub);
        addressEntity.setPath(xPubPath);
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
