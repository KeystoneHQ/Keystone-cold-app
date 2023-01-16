package com.keystone.cold.remove_wallet_mode.helper;

import android.text.TextUtils;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;
import com.keystone.cold.viewmodel.AddAddressViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class PreGenerateAddressHelper {

    private static final DataRepository repository = MainApplication.getApplication().getRepository();
    private volatile static boolean processing = false;

    public static void preGenerateAddress() {
        if (processing) {
            return;
        }
        processing = true;
        AppExecutors.getInstance().networkIO().execute(() -> {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            preGenerateEthDerivationAddress(masterFingerPrint);
            preGenerateSolDerivationAddress(masterFingerPrint);
            preGenerateNearDerivationAddress(masterFingerPrint);
        });

    }

    private static void preGenerateEthDerivationAddress(String masterFingerPrint) {
        String preDerivationPaths = Utilities.getEthDerivationPaths(MainApplication.getApplication());
        if (!TextUtils.isEmpty(preDerivationPaths)) {
            try {
                JSONObject jsonObject = new JSONObject(preDerivationPaths);
                String preMasterFingerprint = jsonObject.optString("master_fingerprint");
                JSONObject accountPaths = jsonObject.optJSONObject("eth_derivation_paths");
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())) {
                    return;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        try {
            JSONObject derivationPaths = new JSONObject();
            JSONObject accountPaths = new JSONObject();
            Arrays.stream(ETHAccount.values()).forEach(ethAccount -> {
                JSONArray addresses = new JSONArray();
                AccountEntity accountEntity = repository.loadTargetETHAccount(ethAccount);
                if (accountEntity != null) {
                    int addressLength = 3;
                    for (int index = 0; index < addressLength; index++) {
                        String address = EthereumAddressGenerator.getAddress(index, ethAccount.getCode());
                        addresses.put(address);
                    }
                    try {
                        accountPaths.put(ethAccount.getCode(), addresses);
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            derivationPaths.put("eth_derivation_paths", accountPaths);
            derivationPaths.put("version", 1);
            derivationPaths.put("master_fingerprint", masterFingerPrint);
            Utilities.setEthDerivationPaths(MainApplication.getApplication(), derivationPaths.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    private static void preGenerateSolDerivationAddress(String masterFingerPrint) {
        String preDerivationPaths = Utilities.getSolDerivationPaths(MainApplication.getApplication());
        if (!TextUtils.isEmpty(preDerivationPaths)) {
            try {
                JSONObject jsonObject = new JSONObject(preDerivationPaths);
                String preMasterFingerprint = jsonObject.optString("master_fingerprint");
                JSONObject accountPaths = jsonObject.optJSONObject("sol_derivation_paths");
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())) {
                    return;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        try {
            JSONObject derivationPaths = new JSONObject();
            JSONObject accountPaths = new JSONObject();
            Arrays.stream(SOLAccount.values()).forEach(solAccount -> {
                JSONArray addresses = new JSONArray();
                AccountEntity accountEntity = repository.loadTargetSOLAccount(solAccount);
                if (accountEntity != null) {
                    int addressLength = 3;
                    if (solAccount == SOLAccount.SOLFLARE_BIP44_ROOT) {
                        addressLength = 1;
                    }
                    for (int index = 0; index < addressLength; index++) {
                        String address = SolanaAddressGenerator.getAddress(index, solAccount.getCode());
                        addresses.put(address);
                    }
                    try {
                        accountPaths.put(solAccount.getCode(), addresses);
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            derivationPaths.put("sol_derivation_paths", accountPaths);
            derivationPaths.put("version", 1);
            derivationPaths.put("master_fingerprint", masterFingerPrint);
            Utilities.setSolDerivationPaths(MainApplication.getApplication(), derivationPaths.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }


    private static void preGenerateNearDerivationAddress(String masterFingerPrint) {
        String preDerivationPaths = Utilities.getNearDerivationPaths(MainApplication.getApplication());
        if (!TextUtils.isEmpty(preDerivationPaths)) {
            try {
                JSONObject jsonObject = new JSONObject(preDerivationPaths);
                String preMasterFingerprint = jsonObject.optString("master_fingerprint");
                JSONObject accountPaths = jsonObject.optJSONObject("near_derivation_paths");
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())) {
                    return;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        try {
            JSONObject derivationPaths = new JSONObject();
            JSONObject accountPaths = new JSONObject();
            NEARAccount[] nearAccounts = new NEARAccount[]{NEARAccount.MNEMONIC, NEARAccount.LEDGER};
            for (int i = 0; i < nearAccounts.length; i++) {
                JSONArray addresses = new JSONArray();
                AccountEntity accountEntity = repository.loadTargetNearAccount(nearAccounts[i]);
                if (accountEntity == null) {
                    continue;
                }
                int addressLength = 3;
                if (nearAccounts[i] == NEARAccount.MNEMONIC) {
                    addressLength = 1;
                }
                for (int index = 0; index < addressLength; index++) {
                    String address = AddAddressViewModel.deriveNearAddress(accountEntity, index, null);
                    addresses.put(address);
                }
                accountPaths.put(nearAccounts[i].getCode(), addresses);
            }
            derivationPaths.put("near_derivation_paths", accountPaths);
            derivationPaths.put("version", 1);
            derivationPaths.put("master_fingerprint", masterFingerPrint);
            Utilities.setNearDerivationPaths(MainApplication.getApplication(), derivationPaths.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
