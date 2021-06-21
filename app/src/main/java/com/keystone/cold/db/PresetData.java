/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.db;

import android.content.Context;

import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;

import java.util.List;
import java.util.stream.Collectors;

import static com.keystone.coinlib.utils.Coins.DOT;
import static com.keystone.coinlib.utils.Coins.KSM;

public class PresetData {

    public static List<CoinEntity> generateCoins(Context context) {
        return Coins.SUPPORTED_COINS.stream()
                .map(coin -> mapToCoinEntity(context, coin))
                .collect(Collectors.toList());
    }

    private static CoinEntity mapToCoinEntity(Context context, Coins.Coin coin) {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(coin.coinId());
        entity.setName(coin.coinName());
        entity.setCoinCode(coin.coinCode());
        entity.setIndex(coin.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(context));
        entity.setAddressCount(0);
        AccountEntity account = new AccountEntity();
        String defaultHdPath = CoinPath.M()
                .purpose(Coins.purposeNumber(entity.getCoinCode()))
                .coinType(entity.getIndex())
                .account(0)
                .toString();

        if (Coins.isPolkadotFamily(coin.coinCode())) {
            defaultHdPath = coin.getAccounts()[0];
        } else if (Coins.CURVE.ED25519 == getCurveByPath(defaultHdPath)) {
            defaultHdPath += "/0'/0'";
        }
        account.setHdPath(defaultHdPath);
        entity.addAccount(account);
        return entity;
    }

    public static Coins.CURVE getCurveByPath(String pubKeyPath) {
        if (pubKeyPath.equals(DOT.getAccounts()[0])
                || pubKeyPath.equals(KSM.getAccounts()[0])) {
            return Coins.CURVE.SR25519;
        }
        String[] strs = pubKeyPath.split("/");
        int coinIndex;
        if (strs[2].endsWith("'")) {
            coinIndex = Integer.parseInt(strs[2].substring(0, strs[2].length() - 1));
        } else {
            coinIndex = Integer.parseInt(strs[2]);
        }
        return Coins.curveFromCoinCode(Coins.coinCodeOfIndex(coinIndex));
    }
}
