package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.remove_wallet_mode.ui.model.PathPatternItem;
import com.keystone.cold.viewmodel.AddAddressViewModel;

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
        }
    }

    public LiveData<List<PathPatternItem>> getPathPattern(String coinId) {
        MutableLiveData<List<PathPatternItem>> patternItemMutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
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
                PathPatternItem pathPatternItem = new PathPatternItem(ethAccount.getCode(), ethAccount.getDisplayPath(), ethAccount.getName(), isRecommend, getEthPairs(ethAccount), isSelected);
                pathPatternItems.add(pathPatternItem);
            }
            return pathPatternItems;
        } else if (Coins.SOL.coinId().equals(coinId)) {

        } else if (Coins.NEAR.coinId().equals(coinId)) {

        }

        return null;
    }


    private List<Pair<String, String>> getEthPairs(ETHAccount ethAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(i, Pair.create("" + i, EthereumAddressGenerator.getAddress(i, ethAccount.getCode())));
        }
        return result;
    }

    private MutableLiveData<List<Pair<String, String>>> getSolAccounts(SOLAccount solAccount) {
        MutableLiveData<List<Pair<String, String>>> mutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Pair<String, String>> result = new ArrayList<>();
            String solDerivationPath = Utilities.getSolDerivationPaths(getApplication());
            if (!TextUtils.isEmpty(solDerivationPath)) {
                String masterFingerPrint = new GetMasterFingerprintCallable().call();
                try {
                    JSONObject derivationPaths = new JSONObject(solDerivationPath);
                    String preMasterFingerPrint = derivationPaths.optString("master_fingerprint");
                    if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint)) {
                        JSONObject accountPaths = (JSONObject) derivationPaths.get("sol_derivation_paths");
                        JSONArray jsonArray = (JSONArray) accountPaths.get(solAccount.getCode());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                        }
                    } else {
                        result.addAll(getPairs(solAccount));
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                result.addAll(getPairs(solAccount));
            }
            mutableLiveData.postValue(result);
        });
        return mutableLiveData;
    }


    private List<Pair<String, String>> getPairs(SOLAccount solAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        AccountEntity accountEntity = repository.loadTargetSOLAccount(solAccount);
        if (accountEntity != null) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, AddAddressViewModel.deriveSolAddress(accountEntity, i, null)));
            }
        }
        return result;
    }

    private MutableLiveData<List<Pair<String, String>>> getNearAccounts(NEARAccount nearAccount) {
        MutableLiveData<List<Pair<String, String>>> mutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Pair<String, String>> result = new ArrayList<>();
            String nearDerivationPath = Utilities.getNearDerivationPaths(getApplication());
            if (!TextUtils.isEmpty(nearDerivationPath)) {
                String masterFingerPrint = new GetMasterFingerprintCallable().call();
                try {
                    JSONObject derivationPaths = new JSONObject(nearDerivationPath);
                    String preMasterFingerPrint = derivationPaths.optString("master_fingerprint");
                    if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint)) {
                        JSONObject accountPaths = (JSONObject) derivationPaths.get("near_derivation_paths");
                        JSONArray jsonArray = (JSONArray) accountPaths.get(nearAccount.getCode());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                        }
                    } else {
                        result.addAll(getPairs(nearAccount));
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                result.addAll(getPairs(nearAccount));
            }
            mutableLiveData.postValue(result);
        });
        return mutableLiveData;
    }

    private List<Pair<String, String>> getPairs(NEARAccount nearAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        AccountEntity accountEntity = repository.loadTargetNearAccount(nearAccount);
        if (accountEntity != null) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, AddAddressViewModel.deriveNearAddress(accountEntity, i, null)));
            }
        }
        return result;
    }

}
