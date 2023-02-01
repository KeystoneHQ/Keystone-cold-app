package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;

import org.json.JSONException;
import org.json.JSONObject;

public class SubstrateAddressGenerator extends BaseAddressGenerator {
    public static String CHAIN_DOT = "polkadot";
    public static String CHAIN_KSM = "kusama";

    private String rootPath; // path: //polkadot | //kusama | etc..

    public SubstrateAddressGenerator(String chain) {
        this.rootPath = "//" + chain;
    }

    public static SubstrateAddressGenerator factory(String chain) {
        return new SubstrateAddressGenerator(chain);
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String path = rootPath;
        if (index != 0) {
            path = rootPath + "/" + index;
        }
        String xPub = new GetExtendedPublicKeyCallable(path).call();
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
