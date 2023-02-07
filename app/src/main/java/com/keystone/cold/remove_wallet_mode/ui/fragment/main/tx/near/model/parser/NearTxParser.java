package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.parser;

import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.NearTx;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.Action;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.AddKey;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.CreateAccount;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.DeleteAccount;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.DeleteKey;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.DeployContract;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.FunctionCall;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.Stake;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.Transfer;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey.AccessKey;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey.FullAccess;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey.FunctionCallPermission;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey.KeyPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class NearTxParser {

    public static NearTx parse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            String signerId = root.optString("signer_id");
            String receiverId = root.optString("receiver_id");
            String publicKey = root.optString("public_key");
            long nonce = root.optLong("nonce");
            JSONArray actions = root.optJSONArray("actions");
            List<Action> actionList = getActions(actions);
            NearTx nearTx = new NearTx();
            nearTx.setSignerId(signerId);
            nearTx.setReceiverId(receiverId);
            nearTx.setPublicKey(publicKey);
            nearTx.setNonce(nonce);
            nearTx.setActions(actionList);
            return nearTx;
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static List<Action> getActions(JSONArray actionJsonArray) throws JSONException {
        if (actionJsonArray == null) {
            return null;
        }
        List<Action> actionList = new ArrayList<>();
        for (int i = 0; i < actionJsonArray.length(); i++) {
            JSONObject object = actionJsonArray.getJSONObject(i);
            Action action = getAction(object);
            actionList.add(action);
        }
        actionList.sort(Comparator.comparingInt(Action::getPriority));
        return actionList;
    }


    private static Action getAction(JSONObject action) throws JSONException {
        Iterator<String> iterator = action.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            switch (key) {
                case "CreateAccount": {
                    return new CreateAccount();
                }
                case "DeployContract": {
                    return new DeployContract();
                }
                case "FunctionCall": {
                    JSONObject object = action.getJSONObject(key);
                    Object argsObject = object.get("args");
                    String args;
                    if (argsObject instanceof JSONArray) {
                        args = object.getJSONArray("args").toString();
                    } else {
                        args = object.getString("args");
                    }
                    String deposit = object.getString("deposit");
                    long gas = object.getLong("gas");
                    String methodName = object.getString("method_name");
                    FunctionCall functionCall = new FunctionCall();
                    functionCall.setArgs(args);
                    functionCall.setDeposit(deposit);
                    functionCall.setGas(gas);
                    functionCall.setMethod_name(methodName);
                    return functionCall;
                }
                case "Transfer": {
                    String deposit = action.getJSONObject(key).getString("deposit");
                    Transfer transfer = new Transfer();
                    transfer.setDeposit(deposit);
                    return transfer;
                }
                case "Stake": {
                    String stakeValue = action.getJSONObject(key).getString("stake");
                    String publicKey = action.getJSONObject(key).getString("public_key");
                    Stake stake = new Stake();
                    stake.setStake(stakeValue);
                    stake.setPublicKey(publicKey);
                    return stake;
                }
                case "AddKey": {
                    String publicKey = action.getJSONObject(key).getString("public_key");
                    JSONObject accessKeyObject = action.getJSONObject(key).getJSONObject("access_key");
                    long nonce = accessKeyObject.getLong("nonce");
                    Object permission = accessKeyObject.get("permission");

                    KeyPermission keyPermission;
                    if (permission instanceof JSONObject) {
                        JSONObject functionCallObject = accessKeyObject.getJSONObject("permission").getJSONObject("FunctionCall");
                        String allowance = functionCallObject.getString("allowance");
                        String receiverId = functionCallObject.getString("receiver_id");
                        String methodNames = functionCallObject.getJSONArray("method_names").toString();
                        if (methodNames.equals("[]")) {
                            methodNames = "null";
                        }
                        FunctionCallPermission functionCallPermission = new FunctionCallPermission();
                        functionCallPermission.setAllowance(allowance);
                        functionCallPermission.setReceiverId(receiverId);
                        functionCallPermission.setMethodNames(methodNames);
                        keyPermission = functionCallPermission;
                    } else {
                        keyPermission = new FullAccess();
                    }

                    AccessKey accessKey = new AccessKey();
                    accessKey.setNonce(nonce);
                    accessKey.setKeyPermission(keyPermission);

                    AddKey addKey = new AddKey();
                    addKey.setPublicKey(publicKey);
                    addKey.setAccessKey(accessKey);

                    return addKey;
                }
                case "DeleteKey": {
                    String publicKey = action.getJSONObject(key).getString("public_key");
                    DeleteKey deleteKey = new DeleteKey();
                    deleteKey.setPublicKey(publicKey);
                    return deleteKey;
                }
                case "DeleteAccount": {
                    String beneficiaryId = action.getJSONObject(key).getString("beneficiary_id");
                    DeleteAccount deleteAccount = new DeleteAccount();
                    deleteAccount.setBeneficiaryId(beneficiaryId);
                    return deleteAccount;
                }
            }

        }
        return null;
    }
}
