package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.cosmos.AddressCodec;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class CosmosAddressGenerator extends BaseAddressGenerator {

    private Coins.Coin coin;

    public CosmosAddressGenerator(Coins.Coin coin) {
        this.coin = coin;
        this.coinId = coin.coinId();
    }


    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String xPubPath = "M/44'/" + coin.coinIndex() + "'/0'/0/" + index;
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(xPubPath);
        String address = deriver.derive(xPub);
        addressEntity.setPath(xPubPath);
        try {
            JSONObject innerJson = new JSONObject();
            innerJson.put("xPub", xPub);
            if (coin.coinCode().equals(Coins.EVMOS.coinCode())) {
                String[] addresses = address.split(AddressCodec.SEPARATOR);
                if (addresses.length == 2) {
                    address = addresses[0];
                    String ethAddress = addresses[1];
                    innerJson.put("ethHexAddress", ethAddress);
                }
            }
            JSONObject addition = new JSONObject();
            addition.put("addition", innerJson);
            addressEntity.setAddition(addition.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return address;
    }
}
