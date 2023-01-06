package com.keystone.cold.remove_wallet_mode.helper.setup;

import android.text.TextUtils;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCreator extends Creator {

    private static final Map<String, String> EXTENDED_PUBLIC_KEY_CACHE = new HashMap<>();
    protected final DataRepository repository = MainApplication.getApplication().getRepository();
    protected Coins.Coin coin;

    enum NextStep {
        CREATE, UPDATE, DO_NOTHING
    }

    public BaseCreator(Coins.Coin coin) {
        this.coin = coin;
    }

    @Override
    public void setUp() {
        NextStep nextStep = judgeNextStep(coin);
        switch (nextStep) {
            case CREATE:
                create();
                break;
            case UPDATE:
                update();
                break;
            case DO_NOTHING:
        }
    }

    protected abstract void update();

    private void create() {
        CoinEntity coinEntity = mapToCoinEntity(coin);
        boolean isFirstAccount = true;
        for (AccountEntity account : coinEntity.getAccounts()) {
            String xPub = getExtendedPublicKey(account.getHdPath());
            if (TextUtils.isEmpty(xPub)) {
                continue;
            }
            if (isFirstAccount) {
                coinEntity.setExPub(xPub);
                isFirstAccount = false;
            }
            account.setExPub(xPub);
        }
        long id = repository.insertCoin(coinEntity);
        for (AccountEntity account : coinEntity.getAccounts()) {
            account.setCoinId(id);
        }
        repository.insertAccounts(coinEntity.getAccounts());

        generateDefaultAddress(coinEntity);
    }

    protected abstract void generateDefaultAddress(CoinEntity coinEntity);

    private NextStep judgeNextStep(Coins.Coin coin) {
        CoinEntity coinEntity = repository.loadCoinSync(coin.coinId());
        if (coinEntity == null) {
            return NextStep.CREATE;
        }
        List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
        if (accountEntities.size() != coinEntity.getAccounts().size()) {
            return NextStep.UPDATE;
        }
        return NextStep.DO_NOTHING;
    }

    private CoinEntity mapToCoinEntity(Coins.Coin coin) {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(coin.coinId());
        entity.setName(coin.coinName());
        entity.setCoinCode(coin.coinCode());
        entity.setIndex(coin.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(MainApplication.getApplication()));
        entity.setAddressCount(0);
        entity.setShow(isDefaultOpen(coin.coinCode()));
        addAccount(entity);
        return entity;
    }

    protected abstract void addAccount(CoinEntity entity);

    protected String getExtendedPublicKey(String pubKeyPath) {
        if (EXTENDED_PUBLIC_KEY_CACHE.containsKey(pubKeyPath)) {
            return EXTENDED_PUBLIC_KEY_CACHE.get(pubKeyPath);
        } else {
            String xPub = new GetExtendedPublicKeyCallable(pubKeyPath).call();
            EXTENDED_PUBLIC_KEY_CACHE.put(pubKeyPath, xPub);
            return xPub;
        }
    }

    public static void clearCache() {
        EXTENDED_PUBLIC_KEY_CACHE.clear();
    }


    public static boolean isDefaultOpen(String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "ETH":
                return true;
            default:
                return false;
        }
    }
}
