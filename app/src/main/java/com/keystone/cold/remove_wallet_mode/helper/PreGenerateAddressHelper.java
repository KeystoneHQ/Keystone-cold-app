package com.keystone.cold.remove_wallet_mode.helper;

import android.text.TextUtils;

import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinCoreNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinLegacyAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNestedSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.NearAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class PreGenerateAddressHelper {

    private static final DataRepository repository = MainApplication.getApplication().getRepository();
    private volatile static boolean processing = false;

    public static final String MFP = "master_fingerprint";
    public static final String VERSION = "version";

    public static final int CURRENT_VERSION_BTC = 2;
    public static final int CURRENT_VERSION_ETH = 1;
    public static final int CURRENT_VERSION_SOL = 1;
    public static final int CURRENT_VERSION_NEAR = 1;


    public static void preGenerateAddress() {
        if (processing) {
            return;
        }
        processing = true;
        AppExecutors.getInstance().networkIO().execute(() -> {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            preGenerateBTCDerivationAddress(masterFingerPrint);
            preGenerateEthDerivationAddress(masterFingerPrint);
            preGenerateSolDerivationAddress(masterFingerPrint);
            preGenerateNearDerivationAddress(masterFingerPrint);
        });

    }

    private static void preGenerateBTCDerivationAddress(String masterFingerPrint) {
        String preDerivationPaths = Utilities.getBTCDerivationPaths(MainApplication.getApplication());
        if (!TextUtils.isEmpty(preDerivationPaths)) {
            try {
                JSONObject jsonObject = new JSONObject(preDerivationPaths);
                String preMasterFingerprint = jsonObject.optString(MFP);
                int version = jsonObject.optInt(VERSION);
                JSONObject accountPaths = jsonObject.optJSONObject(Utilities.BTC_DERIVATION_PATHS);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())
                        && version == CURRENT_VERSION_BTC) {
                    return;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        try {
            JSONObject derivationPaths = new JSONObject();
            JSONObject accountPaths = new JSONObject();
            Arrays.stream(BTCAccount.values()).forEach(btcAccount -> {
                JSONArray addresses = new JSONArray();
                AccountEntity accountEntity = repository.loadTargetBTCAccount(btcAccount);
                if (accountEntity != null) {
                    int addressLength = 3;
                    for (int index = 0; index < addressLength; index++) {
                        String address;
                        if (btcAccount.equals(BTCAccount.LEGACY)) {
                            address = BitcoinLegacyAddressGenerator.getAddress(index);
                        } else if (btcAccount.equals(BTCAccount.NESTED_SEGWIT)) {
                            address = BitcoinNestedSegwitAddressGenerator.getAddress(index);
                        } else if (btcAccount.equals(BTCAccount.NATIVE_SEGWIT)) {
                            address = BitcoinNativeSegwitAddressGenerator.getAddress(index);
                        } else {
                            address = BitcoinCoreNativeSegwitAddressGenerator.getAddress(index);
                        }
                        addresses.put(address);
                    }
                    try {
                        accountPaths.put(btcAccount.getCode(), addresses);
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            derivationPaths.put(Utilities.BTC_DERIVATION_PATHS, accountPaths);
            derivationPaths.put(VERSION, CURRENT_VERSION_BTC);
            derivationPaths.put(MFP, masterFingerPrint);
            Utilities.setBTCDerivationPaths(MainApplication.getApplication(), derivationPaths.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    private static void preGenerateEthDerivationAddress(String masterFingerPrint) {
        String preDerivationPaths = Utilities.getEthDerivationPaths(MainApplication.getApplication());
        if (!TextUtils.isEmpty(preDerivationPaths)) {
            try {
                JSONObject jsonObject = new JSONObject(preDerivationPaths);
                String preMasterFingerprint = jsonObject.optString(MFP);
                int version = jsonObject.optInt(VERSION);
                JSONObject accountPaths = jsonObject.optJSONObject(Utilities.ETH_DERIVATION_PATHS);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())
                        && version == CURRENT_VERSION_ETH) {
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
            derivationPaths.put(Utilities.ETH_DERIVATION_PATHS, accountPaths);
            derivationPaths.put(VERSION, CURRENT_VERSION_ETH);
            derivationPaths.put(MFP, masterFingerPrint);
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
                String preMasterFingerprint = jsonObject.optString(MFP);
                int version = jsonObject.optInt(VERSION);
                JSONObject accountPaths = jsonObject.optJSONObject(Utilities.SOL_DERIVATION_PATHS);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())
                        && version == CURRENT_VERSION_SOL) {
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
            derivationPaths.put(Utilities.SOL_DERIVATION_PATHS, accountPaths);
            derivationPaths.put(VERSION, CURRENT_VERSION_SOL);
            derivationPaths.put(MFP, masterFingerPrint);
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
                String preMasterFingerprint = jsonObject.optString(MFP);
                int version = jsonObject.optInt(VERSION);
                JSONObject accountPaths = jsonObject.optJSONObject(Utilities.NEAR_DERIVATION_PATHS);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                        && accountPaths != null
                        && !TextUtils.isEmpty(accountPaths.toString())
                        && !accountPaths.toString().equals(new JSONObject().toString())
                        && version == CURRENT_VERSION_NEAR) {
                    return;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        try {
            JSONObject derivationPaths = new JSONObject();
            JSONObject accountPaths = new JSONObject();
            Arrays.stream(NEARAccount.values()).forEach(nearAccount -> {
                JSONArray addresses = new JSONArray();
                AccountEntity accountEntity = repository.loadTargetNearAccount(nearAccount);
                if (accountEntity != null) {
                    int addressLength = 3;
                    if (nearAccount == NEARAccount.MNEMONIC) {
                        addressLength = 1;
                    }
                    for (int index = 0; index < addressLength; index++) {
                        String address = NearAddressGenerator.getAddress(index, nearAccount.getCode());
                        addresses.put(address);
                    }
                    try {
                        accountPaths.put(nearAccount.getCode(), addresses);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            derivationPaths.put(Utilities.NEAR_DERIVATION_PATHS, accountPaths);
            derivationPaths.put(VERSION, CURRENT_VERSION_NEAR);
            derivationPaths.put(MFP, masterFingerPrint);
            Utilities.setNearDerivationPaths(MainApplication.getApplication(), derivationPaths.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
