package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NearAddressGenerator extends MultiAccountAddressGenerator {

    public NearAddressGenerator(String code) {
        super(code);
        coinId = Coins.NEAR.coinId();
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        NEARAccount nearAccount = NEARAccount.ofCode(code);
        String path = null;
        switch (nearAccount) {
            case MNEMONIC:
                path = "M/44'/397'/0'";
                break;
            case LEDGER:
                path = "M/44'/397'/0'/0'/" + index + "'";
                break;
        }
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(path);
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);

        try {
            JSONObject innerJson = new JSONObject();
            innerJson.put("derivation_path", nearAccount.getCode());
            innerJson.put("index", index);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(innerJson);
            JSONObject addition = new JSONObject();
            addition.put("addition", jsonArray);
            addressEntity.setAddition(addition.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return address;
    }

    @Override
    protected AccountEntity getAccountByRule(List<AccountEntity> accountEntities) {
        NEARAccount account = NEARAccount.ofCode(code);
        try {
            for (int i = 0; i < accountEntities.size(); i++) {
                JSONObject jsonObject = new JSONObject(accountEntities.get(i).getAddition());
                if (jsonObject.get("near_account").equals(account.getCode())) {
                    return accountEntities.get(i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAddress(int index, String code) {
        NEARAccount nearAccount = NEARAccount.ofCode(code);
        String path = null;
        switch (nearAccount) {
            case MNEMONIC:
                path = "M/44'/397'/0'";
                break;
            case LEDGER:
                path = "M/44'/397'/0'/0'/" + index + "'";
                break;
        }
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        AbsDeriver deriver = AbsDeriver.newInstance(Coins.NEAR.coinCode());
        assert deriver != null;
        return deriver.derive(xPub);
    }
}
