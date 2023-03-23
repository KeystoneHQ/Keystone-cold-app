package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.remove_wallet_mode.helper.PreGenerateAddressHelper;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinCoreNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinLegacyAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNestedSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.NearAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChangePathViewModel extends AndroidViewModel {

    private final DataRepository repository;

    public ChangePathViewModel(@NonNull Application application) {
        super(application);
        repository = ((MainApplication) application).getRepository();
    }

    public void save(String coinId, String code) {
        if (coinId.equals(Coins.ETH.coinId())) {
            if (!TextUtils.isEmpty(code)) {
                Utilities.setCurrentEthAccount(getApplication(), code);
            }
        } else if (coinId.equals(Coins.SOL.coinId())) {
            if (!TextUtils.isEmpty(code)) {
                Utilities.setCurrentSolAccount(getApplication(), code);
            }
        } else if (coinId.equals(Coins.NEAR.coinId())) {
            if (!TextUtils.isEmpty(code)) {
                Utilities.setCurrentNearAccount(getApplication(), code);
            }
        } else if (coinId.equals(Coins.BTC.coinId())) {
            if (!TextUtils.isEmpty(code)) {
                Utilities.setCurrentBTCAccount(getApplication(), code);
            }
        }
    }

    public LiveData<List<PathPatternItem>> getPathPattern(String coinId) {
        MutableLiveData<List<PathPatternItem>> patternItemMutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            patternItemMutableLiveData.postValue(getPathPatternData(coinId));
        });

        return patternItemMutableLiveData;
    }

    private List<PathPatternItem> getPathPatternData(String coinId) {
        List<PathPatternItem> pathPatternItems = new ArrayList<>();
        if (Coins.ETH.coinId().equals(coinId)) {
            String code = Utilities.getCurrentEthAccount(getApplication());
            ETHAccount account = ETHAccount.ofCode(code);
            for (ETHAccount ethAccount : ETHAccount.values()) {
                boolean isSelected = ethAccount == account;
                boolean isRecommend = ethAccount.getName().equals(ETHAccount.BIP44_STANDARD.getName());
                PathPatternItem pathPatternItem = new PathPatternItem(ethAccount.getCode(), ethAccount.getDisplayPath(), ethAccount.getName(), isRecommend, getEthAccountAddresses(ethAccount), isSelected);
                if (ethAccount.equals(ETHAccount.BIP44_STANDARD)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_eth_standard));
                } else if (ethAccount.equals(ETHAccount.LEDGER_LEGACY)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_eth_ledger_legacy));
                } else {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_eth_ledger_live));
                }
                pathPatternItems.add(pathPatternItem);
            }
            return pathPatternItems;
        } else if (Coins.SOL.coinId().equals(coinId)) {
            String code = Utilities.getCurrentSolAccount(getApplication());
            SOLAccount account = SOLAccount.ofCode(code);
            for (SOLAccount solAccount : SOLAccount.values()) {
                boolean isSelected = solAccount == account;
                boolean isRecommend = solAccount.getName().equals(SOLAccount.SOLFLARE_BIP44.getName());
                PathPatternItem pathPatternItem = new PathPatternItem(solAccount.getCode(), solAccount.getDisplayPath(), null, isRecommend, getSolAccountAddresses(solAccount), isSelected);
                if (solAccount.equals(SOLAccount.SOLFLARE_BIP44)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_sol_standard));
                } else if (solAccount.equals(SOLAccount.SOLFLARE_BIP44_ROOT)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_sol_root));
                } else {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_sol_change));
                }
                pathPatternItems.add(pathPatternItem);
            }
            return pathPatternItems;
        } else if (Coins.NEAR.coinId().equals(coinId)) {
            String code = Utilities.getCurrentNearAccount(getApplication());
            NEARAccount account = NEARAccount.ofCode(code);
            for (NEARAccount nearAccount : NEARAccount.values()) {
                boolean isSelected = nearAccount == account;
                boolean isRecommend = nearAccount.getName().equals(NEARAccount.MNEMONIC.getName());
                PathPatternItem pathPatternItem = new PathPatternItem(nearAccount.getCode(), nearAccount.getDisplayPath(), nearAccount.getName(), isRecommend, getNearAccountAddresses(nearAccount), isSelected);
                if (nearAccount.equals(NEARAccount.MNEMONIC)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_near_mnemonic));
                } else {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_near_ledger_live));
                }
                pathPatternItems.add(pathPatternItem);
            }
            return pathPatternItems;
        } else if (Coins.BTC.coinId().equals(coinId)) {
            String code = Utilities.getCurrentBTCAccount(getApplication());
            BTCAccount account = BTCAccount.ofCode(code);
            for (BTCAccount btcAccount : BTCAccount.values()) {
//                if (btcAccount.equals(BTCAccount.CORE_NATIVE_SEGWIT)) {
//                    continue;
//                }
                boolean isSelected = btcAccount.equals(account);
                boolean isRecommend = btcAccount.equals(BTCAccount.NATIVE_SEGWIT);
                PathPatternItem pathPatternItem = new PathPatternItem(btcAccount.getCode(), btcAccount.getDisplayPath(), btcAccount.getName(), isRecommend, getBTCAccountAddresses(btcAccount), isSelected);
                if (btcAccount.equals(BTCAccount.NATIVE_SEGWIT)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_btc_native_segwit));
                } else if (btcAccount.equals(BTCAccount.NESTED_SEGWIT)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_btc_nested_segwit));
                } else if (btcAccount.equals(BTCAccount.LEGACY)) {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_btc_legacy));
                } else {
                    pathPatternItem.setDescription(getApplication().getString(R.string.derivation_path_btc_core_wallet));
                }
                pathPatternItems.add(pathPatternItem);
            }
            return pathPatternItems;
        }

        return null;
    }

    private List<Pair<String, String>> getEthAccountAddresses(ETHAccount ethAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        String ethDerivationPath = Utilities.getEthDerivationPaths(getApplication());
        if (!TextUtils.isEmpty(ethDerivationPath)) {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            try {
                JSONObject derivationPaths = new JSONObject(ethDerivationPath);
                String preMasterFingerPrint = derivationPaths.optString(PreGenerateAddressHelper.MFP);
                int version = derivationPaths.optInt(PreGenerateAddressHelper.VERSION);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint) && version == PreGenerateAddressHelper.CURRENT_VERSION_ETH) {
                    JSONObject accountPaths = (JSONObject) derivationPaths.get(Utilities.ETH_DERIVATION_PATHS);
                    JSONArray jsonArray = (JSONArray) accountPaths.get(ethAccount.getCode());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                    }
                } else {
                    result.addAll(getEthPairs(ethAccount));
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        } else {
            result.addAll(getEthPairs(ethAccount));
        }
        return result;
    }

    private List<Pair<String, String>> getEthPairs(ETHAccount ethAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(i, Pair.create("" + i, EthereumAddressGenerator.getAddress(i, ethAccount.getCode())));
        }
        return result;
    }

    private List<Pair<String, String>> getBTCAccountAddresses(BTCAccount btcAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        String btcDerivationPaths = Utilities.getBTCDerivationPaths(getApplication());
        if (!TextUtils.isEmpty(btcDerivationPaths)) {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            try {
                JSONObject derivationPaths = new JSONObject(btcDerivationPaths);
                String preMasterFingerPrint = derivationPaths.optString(PreGenerateAddressHelper.MFP);
                int version = derivationPaths.optInt(PreGenerateAddressHelper.VERSION);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint) && version == PreGenerateAddressHelper.CURRENT_VERSION_BTC) {
                    JSONObject accountPaths = (JSONObject) derivationPaths.get(Utilities.BTC_DERIVATION_PATHS);
                    JSONArray jsonArray = (JSONArray) accountPaths.get(btcAccount.getCode());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                    }
                } else {
                    result.addAll(getBTCPairs(btcAccount));
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        } else {
            result.addAll(getBTCPairs(btcAccount));
        }
        return result;
    }

    private List<Pair<String, String>> getBTCPairs(BTCAccount btcAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        if (btcAccount.getCode().equals(BTCAccount.LEGACY.getCode())) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, BitcoinLegacyAddressGenerator.getAddress(i)));
            }
        } else if (btcAccount.getCode().equals(BTCAccount.NESTED_SEGWIT.getCode())) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, BitcoinNestedSegwitAddressGenerator.getAddress(i)));
            }
        } else if (btcAccount.getCode().equals(BTCAccount.NATIVE_SEGWIT.getCode())) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, BitcoinNativeSegwitAddressGenerator.getAddress(i)));
            }
        } else {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, BitcoinCoreNativeSegwitAddressGenerator.getAddress(i)));
            }
        }
        return result;
    }

    private List<Pair<String, String>> getSolAccountAddresses(SOLAccount solAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        String solDerivationPath = Utilities.getSolDerivationPaths(getApplication());
        if (!TextUtils.isEmpty(solDerivationPath)) {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            try {
                JSONObject derivationPaths = new JSONObject(solDerivationPath);
                String preMasterFingerPrint = derivationPaths.optString(PreGenerateAddressHelper.MFP);
                int version = derivationPaths.optInt(PreGenerateAddressHelper.VERSION);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint) && version == PreGenerateAddressHelper.CURRENT_VERSION_SOL) {
                    JSONObject accountPaths = (JSONObject) derivationPaths.get(Utilities.SOL_DERIVATION_PATHS);
                    JSONArray jsonArray = (JSONArray) accountPaths.get(solAccount.getCode());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                    }
                } else {
                    result.addAll(getSolPairs(solAccount));
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        } else {
            result.addAll(getSolPairs(solAccount));
        }
        return result;
    }


    private List<Pair<String, String>> getSolPairs(SOLAccount solAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        AccountEntity accountEntity = repository.loadTargetSOLAccount(solAccount);
        if (accountEntity != null) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, SolanaAddressGenerator.getAddress(i, solAccount.getCode())));
            }
        }
        return result;
    }

    private List<Pair<String, String>> getNearAccountAddresses(NEARAccount nearAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        String nearDerivationPath = Utilities.getNearDerivationPaths(getApplication());
        if (!TextUtils.isEmpty(nearDerivationPath)) {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            try {
                JSONObject derivationPaths = new JSONObject(nearDerivationPath);
                String preMasterFingerPrint = derivationPaths.optString(PreGenerateAddressHelper.MFP);
                int version = derivationPaths.optInt(PreGenerateAddressHelper.VERSION);
                if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint) && version == PreGenerateAddressHelper.CURRENT_VERSION_NEAR) {
                    JSONObject accountPaths = (JSONObject) derivationPaths.get(Utilities.NEAR_DERIVATION_PATHS);
                    JSONArray jsonArray = (JSONArray) accountPaths.get(nearAccount.getCode());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                    }
                } else {
                    result.addAll(getNearPairs(nearAccount));
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        } else {
            result.addAll(getNearPairs(nearAccount));
        }
        return result;
    }

    private List<Pair<String, String>> getNearPairs(NEARAccount nearAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        AccountEntity accountEntity = repository.loadTargetNearAccount(nearAccount);
        if (accountEntity != null) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, NearAddressGenerator.getAddress(i, nearAccount.getCode())));
            }
        }
        return result;
    }

}
